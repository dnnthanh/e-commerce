package com.dnnthanh.marketplace.be.comment.api.adapter.out.mongo.document;

import com.dnnthanh.marketplace.be.comment.api.domain.enumtype.CommentStatus;
import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/** Mongo source-of-truth document for one comment thread root. */
@Document("comment_thread")
public record CommentThreadDocument(
        @Id String id,
        Long productId,
        Long sellerId,
        String authorId,
        String content,
        CommentStatus status,
        int replyCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {}
