package com.dnnthanh.marketplace.be.comment.api.application.service;

import com.dnnthanh.marketplace.be.comment.api.application.command.CommentReactionCommand;
import com.dnnthanh.marketplace.be.comment.api.application.command.CreateCommentCommand;
import com.dnnthanh.marketplace.be.comment.api.application.command.EditCommentCommand;
import com.dnnthanh.marketplace.be.comment.api.application.command.ReplyCommentCommand;
import com.dnnthanh.marketplace.be.comment.api.application.command.ReportCommentCommand;
import com.dnnthanh.marketplace.be.comment.api.application.exception.CommentConflictException;
import com.dnnthanh.marketplace.be.comment.api.application.exception.CommentForbiddenException;
import com.dnnthanh.marketplace.be.comment.api.application.exception.CommentNotFoundException;
import com.dnnthanh.marketplace.be.comment.api.application.exception.InvalidCommentCommandException;
import com.dnnthanh.marketplace.be.comment.api.application.port.in.CommentCommandUseCase;
import com.dnnthanh.marketplace.be.comment.api.application.port.out.CommentPersistencePort;
import com.dnnthanh.marketplace.be.comment.api.application.port.out.CommentRateLimitPort;
import com.dnnthanh.marketplace.be.comment.api.domain.enumtype.CommentEventType;
import com.dnnthanh.marketplace.be.comment.api.domain.exception.InvalidCommentException;
import com.dnnthanh.marketplace.be.comment.api.domain.exception.InvalidCommentStateException;
import com.dnnthanh.marketplace.be.comment.api.domain.model.CommentReaction;
import com.dnnthanh.marketplace.be.comment.api.domain.model.CommentReply;
import com.dnnthanh.marketplace.be.comment.api.domain.model.CommentReport;
import com.dnnthanh.marketplace.be.comment.api.domain.model.CommentThread;
import com.dnnthanh.marketplace.be.comment.api.domain.model.CommentThreadDraft;
import com.dnnthanh.marketplace.be.comment.api.domain.service.MentionExtractor;
import com.dnnthanh.marketplace.be.platform.context.UserContext;
import com.dnnthanh.marketplace.be.platform.stereotype.UseCase;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

/** Transactional Comment write use cases with anti-spam, authorization, outbox and idempotency. */
@UseCase
@RequiredArgsConstructor
public class CommentCommandServiceImplement implements CommentCommandUseCase {

    private final CommentPersistencePort persistence;
    private final CommentRateLimitPort rateLimit;
    private final MentionExtractor mentionExtractor;
    private final UserContext userContext;
    private final Clock clock;

    @Override
    @Transactional
    public CommentThread create(CreateCommentCommand command) {
        String actorId = currentUserId();
        rateLimit.checkAllowed(actorId);
        CommentThread thread;
        try {
            thread =
                    CommentThread.create(
                            CommentThreadDraft.builder()
                                    .productId(command.productId())
                                    .sellerId(command.sellerId())
                                    .authorId(actorId)
                                    .content(command.content())
                                    .build(),
                            now());
        } catch (InvalidCommentException exception) {
            throw new InvalidCommentCommandException();
        }
        CommentThread saved = persistence.saveThread(thread);
        persistence.appendOutbox(
                CommentEventType.COMMENT_CREATED,
                saved.getId(),
                Map.of(
                        "threadId", saved.getId(),
                        "productId", saved.getProductId(),
                        "authorId", saved.getAuthorId(),
                        "mentions", mentionExtractor.extract(saved.getContent())));
        return saved;
    }

    /** Adds a separately persisted reply and atomically increments the root count. */
    @Override
    @Transactional
    public CommentReply reply(ReplyCommentCommand command) {
        String actorId = currentUserId();
        rateLimit.checkAllowed(actorId);
        CommentThread thread = requireThread(command.threadId());
        CommentReply reply;
        try {
            reply = CommentReply.create(command.threadId(), actorId, command.content(), now());
        } catch (InvalidCommentException exception) {
            throw new InvalidCommentCommandException();
        }
        CommentReply savedReply = persistence.insertReply(reply);
        try {
            thread.recordReply(now());
        } catch (InvalidCommentStateException exception) {
            throw new CommentConflictException();
        }
        persistence.saveThread(thread);
        persistence.appendOutbox(
                CommentEventType.COMMENT_REPLIED,
                command.threadId(),
                Map.of(
                        "threadId", command.threadId(),
                        "replyId", savedReply.id(),
                        "recipientUserId", thread.getAuthorId(),
                        "authorId", actorId,
                        "mentions", mentionExtractor.extract(savedReply.content())));
        return savedReply;
    }

    /** Edits an owned comment. */
    @Override
    @Transactional
    public CommentThread edit(EditCommentCommand command) {
        String actorId = currentUserId();
        CommentThread thread = requireThread(command.threadId());
        requireOwner(thread, actorId);
        try {
            thread.edit(actorId, command.content(), now());
        } catch (InvalidCommentException exception) {
            throw new InvalidCommentCommandException();
        } catch (InvalidCommentStateException exception) {
            throw new CommentConflictException();
        }
        CommentThread saved = persistence.saveThread(thread);
        persistence.appendOutbox(
                CommentEventType.COMMENT_EDITED,
                command.threadId(),
                Map.of("threadId", command.threadId()));
        return saved;
    }

    /** Soft deletes an owned root without cascading to replies. */
    @Override
    @Transactional
    public CommentThread delete(String threadId) {
        String actorId = currentUserId();
        CommentThread thread = requireThread(threadId);
        requireOwner(thread, actorId);
        thread.softDelete(actorId, now());
        CommentThread saved = persistence.saveThread(thread);
        persistence.appendOutbox(
                CommentEventType.COMMENT_DELETED, threadId, Map.of("threadId", threadId));
        return saved;
    }

    /** Hides a thread from normal listing through a moderator-only HTTP endpoint. */
    @Override
    @Transactional
    public CommentThread hide(String threadId) {
        CommentThread thread = requireThread(threadId);
        try {
            thread.hide(now());
        } catch (InvalidCommentStateException exception) {
            throw new CommentConflictException();
        }
        CommentThread saved = persistence.saveThread(thread);
        persistence.appendOutbox(
                CommentEventType.COMMENT_HIDDEN, threadId, Map.of("threadId", threadId));
        return saved;
    }

    /** Restores a moderation-hidden thread. */
    @Override
    @Transactional
    public CommentThread unhide(String threadId) {
        CommentThread thread = requireThread(threadId);
        try {
            thread.unhide(now());
        } catch (InvalidCommentStateException exception) {
            throw new CommentConflictException();
        }
        CommentThread saved = persistence.saveThread(thread);
        persistence.appendOutbox(
                CommentEventType.COMMENT_UNHIDDEN, threadId, Map.of("threadId", threadId));
        return saved;
    }

    @Override
    @Transactional
    public CommentReport report(ReportCommentCommand command) {
        String actorId = currentUserId();
        requireThread(command.threadId());
        rateLimit.checkAllowed(actorId);
        CommentReport saved =
                persistence.insertReport(
                        CommentReport.create(
                                command.threadId(),
                                actorId,
                                command.reason(),
                                command.details(),
                                now()));
        persistence.appendOutbox(
                CommentEventType.COMMENT_REPORTED,
                command.threadId(),
                Map.of(
                        "threadId", command.threadId(),
                        "reportId", saved.id(),
                        "reason", saved.reason()));
        return saved;
    }

    /** Adds an idempotent reaction identified by thread/user/type. */
    @Override
    @Transactional
    public CommentReaction react(CommentReactionCommand command) {
        String actorId = currentUserId();
        requireThread(command.threadId());
        CommentReaction saved =
                persistence.addReaction(
                        CommentReaction.create(command.threadId(), actorId, command.type(), now()));
        persistence.appendOutbox(
                CommentEventType.COMMENT_REACTED,
                command.threadId(),
                Map.of(
                        "threadId", command.threadId(),
                        "userId", actorId,
                        "type", command.type()));
        return saved;
    }

    /** Removes a reaction idempotently. */
    @Override
    @Transactional
    public boolean removeReaction(CommentReactionCommand command) {
        String actorId = currentUserId();
        requireThread(command.threadId());
        boolean removed = persistence.removeReaction(command.threadId(), actorId, command.type());
        if (removed) {
            persistence.appendOutbox(
                    CommentEventType.COMMENT_REACTION_REMOVED,
                    command.threadId(),
                    Map.of(
                            "threadId", command.threadId(),
                            "userId", actorId,
                            "type", command.type()));
        }
        return removed;
    }

    private void requireOwner(CommentThread thread, String requesterId) {
        if (!Objects.equals(thread.getAuthorId(), requesterId)) {
            throw new CommentForbiddenException();
        }
    }

    private CommentThread requireThread(String threadId) {
        return persistence.findThread(threadId).orElseThrow(CommentNotFoundException::new);
    }

    private String currentUserId() {
        return userContext.userId();
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }
}
