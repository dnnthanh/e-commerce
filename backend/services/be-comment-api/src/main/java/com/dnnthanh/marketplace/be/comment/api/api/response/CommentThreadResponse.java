package com.dnnthanh.marketplace.be.comment.api.api.response;

import com.dnnthanh.marketplace.be.comment.api.domain.enumtype.CommentStatus;
import java.time.LocalDateTime;

/** Public comment-thread response; deleted content is masked by the domain model. */
public record CommentThreadResponse(
        String id,
        Long productId,
        Long sellerId,
        String authorId,
        String content,
        CommentStatus status,
        int replyCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {}
