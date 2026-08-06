package com.dnnthanh.marketplace.be.comment.api.adapter.out.mongo;

import com.dnnthanh.marketplace.be.comment.api.adapter.out.mongo.document.CommentOutboxDocument;
import com.dnnthanh.marketplace.be.comment.api.adapter.out.mongo.document.CommentReactionDocument;
import com.dnnthanh.marketplace.be.comment.api.adapter.out.mongo.document.CommentReplyDocument;
import com.dnnthanh.marketplace.be.comment.api.adapter.out.mongo.document.CommentReportDocument;
import com.dnnthanh.marketplace.be.comment.api.adapter.out.mongo.document.CommentThreadDocument;
import com.dnnthanh.marketplace.be.comment.api.adapter.out.mongo.exception.CommentPersistenceException;
import com.dnnthanh.marketplace.be.comment.api.adapter.out.mongo.repository.CommentOutboxMongoRepository;
import com.dnnthanh.marketplace.be.comment.api.adapter.out.mongo.repository.CommentReactionMongoRepository;
import com.dnnthanh.marketplace.be.comment.api.adapter.out.mongo.repository.CommentReplyMongoRepository;
import com.dnnthanh.marketplace.be.comment.api.adapter.out.mongo.repository.CommentReportMongoRepository;
import com.dnnthanh.marketplace.be.comment.api.adapter.out.mongo.repository.CommentThreadMongoRepository;
import com.dnnthanh.marketplace.be.comment.api.application.port.out.CommentPersistencePort;
import com.dnnthanh.marketplace.be.comment.api.application.query.CommentSearchCriteria;
import com.dnnthanh.marketplace.be.comment.api.domain.constant.CommentConstants;
import com.dnnthanh.marketplace.be.comment.api.domain.enumtype.CommentEventType;
import com.dnnthanh.marketplace.be.comment.api.domain.enumtype.CommentStatus;
import com.dnnthanh.marketplace.be.comment.api.domain.enumtype.ReactionType;
import com.dnnthanh.marketplace.be.comment.api.domain.model.CommentReaction;
import com.dnnthanh.marketplace.be.comment.api.domain.model.CommentReply;
import com.dnnthanh.marketplace.be.comment.api.domain.model.CommentReport;
import com.dnnthanh.marketplace.be.comment.api.domain.model.CommentThread;
import com.dnnthanh.marketplace.be.platform.stereotype.Persistence;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import tools.jackson.databind.ObjectMapper;

/** Mongo adapter that keeps persistence mechanics out of Comment application use cases. */
@Persistence
@RequiredArgsConstructor
public class CommentMongoPersistenceAdapter implements CommentPersistencePort {

    private final CommentThreadMongoRepository threadRepository;

    private final CommentReplyMongoRepository replyRepository;

    private final CommentReactionMongoRepository reactionRepository;

    private final CommentReportMongoRepository reportRepository;

    private final CommentOutboxMongoRepository outboxRepository;

    private final ObjectMapper objectMapper;

    @Override
    public Optional<CommentThread> findThread(String threadId) {
        return threadRepository.findById(threadId).map(this::toDomain);
    }

    @Override
    public CommentThread saveThread(CommentThread thread) {
        return toDomain(threadRepository.save(toDocument(thread)));
    }

    @Override
    public CommentReply insertReply(CommentReply reply) {
        CommentReplyDocument saved =
                replyRepository.save(
                        new CommentReplyDocument(
                                reply.id(),
                                reply.threadId(),
                                reply.authorId(),
                                reply.content(),
                                reply.createdAt()));
        return new CommentReply(
                saved.id(), saved.threadId(), saved.authorId(), saved.content(), saved.createdAt());
    }

    @Override
    public Page<CommentThread> findThreads(CommentSearchCriteria criteria, Pageable pageable) {
        return threadRepository
                .findByProductIdAndStatusNot(criteria.productId(), CommentStatus.HIDDEN, pageable)
                .map(this::toDomain);
    }

    @Override
    public Page<CommentReply> findReplies(String threadId, Pageable pageable) {
        return replyRepository
                .findByThreadId(threadId, pageable)
                .map(
                        document ->
                                new CommentReply(
                                        document.id(),
                                        document.threadId(),
                                        document.authorId(),
                                        document.content(),
                                        document.createdAt()));
    }

    @Override
    public CommentReaction addReaction(CommentReaction reaction) {
        CommentReactionDocument saved =
                reactionRepository.save(
                        new CommentReactionDocument(
                                reaction.id(),
                                reaction.threadId(),
                                reaction.userId(),
                                reaction.type(),
                                reaction.createdAt()));
        return new CommentReaction(
                saved.id(), saved.threadId(), saved.userId(), saved.type(), saved.createdAt());
    }

    @Override
    public boolean removeReaction(String threadId, String userId, ReactionType type) {
        return reactionRepository.deleteByThreadIdAndUserIdAndType(threadId, userId, type) > 0;
    }

    @Override
    public CommentReport insertReport(CommentReport report) {
        CommentReportDocument saved =
                reportRepository.save(
                        new CommentReportDocument(
                                report.id(),
                                report.threadId(),
                                report.reporterUserId(),
                                report.reason(),
                                report.details(),
                                report.createdAt()));
        return new CommentReport(
                saved.id(),
                saved.threadId(),
                saved.reporterUserId(),
                saved.reason(),
                saved.details(),
                saved.createdAt());
    }

    @Override
    public void appendOutbox(
            CommentEventType eventType, String aggregateId, Map<String, ?> payload) {
        try {
            outboxRepository.save(
                    new CommentOutboxDocument(
                            UUID.randomUUID().toString(),
                            eventType,
                            aggregateId,
                            objectMapper.writeValueAsString(payload),
                            CommentConstants.OUTBOX_PENDING_STATUS,
                            LocalDateTime.now(),
                            null));
        } catch (Exception exception) {
            throw new CommentPersistenceException(
                    "Unable to serialize comment outbox payload", exception);
        }
    }

    private CommentThreadDocument toDocument(CommentThread thread) {
        return new CommentThreadDocument(
                thread.getId(),
                thread.getProductId(),
                thread.getSellerId(),
                thread.getAuthorId(),
                thread.getContent(),
                thread.getStatus(),
                thread.getReplyCount(),
                thread.getCreatedAt(),
                thread.getUpdatedAt());
    }

    private CommentThread toDomain(CommentThreadDocument document) {
        return CommentThread.rehydrate(
                document.id(),
                document.productId(),
                document.sellerId(),
                document.authorId(),
                document.content(),
                document.status(),
                document.replyCount(),
                document.createdAt(),
                document.updatedAt());
    }
}
