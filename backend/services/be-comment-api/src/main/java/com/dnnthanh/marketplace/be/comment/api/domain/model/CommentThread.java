package com.dnnthanh.marketplace.be.comment.api.domain.model;

import com.dnnthanh.marketplace.be.comment.api.domain.constant.CommentConstants;
import com.dnnthanh.marketplace.be.comment.api.domain.enumtype.CommentStatus;
import com.dnnthanh.marketplace.be.comment.api.domain.exception.CommentPermissionException;
import com.dnnthanh.marketplace.be.comment.api.domain.exception.InvalidCommentException;
import com.dnnthanh.marketplace.be.comment.api.domain.exception.InvalidCommentStateException;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;

/**
 * Product comment/Q&A thread root; replies are separate Mongo documents to avoid unbounded roots.
 */
@Getter
public final class CommentThread {

    private final String id;

    private final Long productId;

    private final Long sellerId;

    private final String authorId;

    private String content;

    private CommentStatus status;

    private int replyCount;

    private final LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private CommentThread(
            String id,
            Long productId,
            Long sellerId,
            String authorId,
            String content,
            CommentStatus status,
            int replyCount,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        this.id = requireText(id, "id");
        this.productId = Objects.requireNonNull(productId, "productId");
        this.sellerId = Objects.requireNonNull(sellerId, "sellerId");
        this.authorId = requireText(authorId, "authorId");
        this.content = validateContent(content);
        this.status = Objects.requireNonNull(status, "status");
        this.replyCount = replyCount;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
    }

    public static CommentThread create(CommentThreadDraft draft, LocalDateTime now) {
        Objects.requireNonNull(draft, "draft");
        return new CommentThread(
                UUID.randomUUID().toString(),
                draft.productId(),
                draft.sellerId(),
                draft.authorId(),
                draft.content(),
                CommentStatus.PUBLISHED,
                0,
                now,
                now);
    }

    /** Rehydrates persisted state. */
    public static CommentThread rehydrate(
            String id,
            Long productId,
            Long sellerId,
            String authorId,
            String content,
            CommentStatus status,
            int replyCount,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        return new CommentThread(
                id,
                productId,
                sellerId,
                authorId,
                content,
                status,
                replyCount,
                createdAt,
                updatedAt);
    }

    /** Edits an owned comment while preserving moderation/deletion invariants. */
    public void edit(String requesterId, String newContent, LocalDateTime now) {
        requireOwner(requesterId);
        if (status == CommentStatus.DELETED || status == CommentStatus.HIDDEN) {
            throw new InvalidCommentStateException("Deleted or hidden comments cannot be edited");
        }
        content = validateContent(newContent);
        status = CommentStatus.EDITED;
        updatedAt = now;
    }

    /** Soft deletes an owned root without deleting descendants. */
    public void softDelete(String requesterId, LocalDateTime now) {
        requireOwner(requesterId);
        if (status == CommentStatus.DELETED) {
            return;
        }
        status = CommentStatus.DELETED;
        updatedAt = now;
    }

    /** Hides the thread through a moderator/admin use case. */
    public void hide(LocalDateTime now) {
        if (status == CommentStatus.DELETED) {
            throw new InvalidCommentStateException("Deleted comments cannot be hidden");
        }
        status = CommentStatus.HIDDEN;
        updatedAt = now;
    }

    /** Restores a moderation-hidden thread. */
    public void unhide(LocalDateTime now) {
        if (status != CommentStatus.HIDDEN) {
            throw new InvalidCommentStateException("Only hidden comments can be unhidden");
        }
        status = CommentStatus.PUBLISHED;
        updatedAt = now;
    }

    /** Records one separately persisted reply. */
    public void recordReply(LocalDateTime now) {
        if (status == CommentStatus.DELETED || status == CommentStatus.HIDDEN) {
            throw new InvalidCommentStateException(
                    "Replies are not allowed for deleted or hidden comments");
        }
        replyCount++;
        updatedAt = now;
    }

    private void requireOwner(String requesterId) {
        if (!Objects.equals(authorId, requesterId)) {
            throw new CommentPermissionException();
        }
    }

    private static String validateContent(String value) {
        String validated = requireText(value, "content");
        if (validated.length() > CommentConstants.MAX_CONTENT_LENGTH) {
            throw new InvalidCommentException("Comment content exceeds maximum length");
        }
        return validated;
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new InvalidCommentException(field + " is required");
        }
        return value;
    }

    /** Content safe to expose publicly while preserving the soft-delete invariant. */
    public String getVisibleContent() {
        return status == CommentStatus.DELETED ? null : content;
    }
}
