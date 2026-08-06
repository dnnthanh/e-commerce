package com.dnnthanh.marketplace.be.comment.api.domain.model;

import com.dnnthanh.marketplace.be.comment.api.domain.constant.CommentConstants;
import com.dnnthanh.marketplace.be.comment.api.domain.exception.InvalidCommentException;
import java.time.LocalDateTime;
import java.util.UUID;

/** Immutable reply document model. */
public record CommentReply(
        String id, String threadId, String authorId, String content, LocalDateTime createdAt) {
    public static CommentReply create(
            String threadId, String authorId, String content, LocalDateTime now) {
        if (threadId == null || threadId.isBlank() || authorId == null || authorId.isBlank()) {
            throw new InvalidCommentException("Reply thread and author are required");
        }
        if (content == null
                || content.isBlank()
                || content.length() > CommentConstants.MAX_CONTENT_LENGTH) {
            throw new InvalidCommentException("Reply content is invalid");
        }
        return new CommentReply(UUID.randomUUID().toString(), threadId, authorId, content, now);
    }
}
