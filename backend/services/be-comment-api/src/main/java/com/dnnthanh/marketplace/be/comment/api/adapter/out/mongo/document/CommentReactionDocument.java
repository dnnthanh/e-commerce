package com.dnnthanh.marketplace.be.comment.api.adapter.out.mongo.document;

import com.dnnthanh.marketplace.be.comment.api.domain.enumtype.ReactionType;
import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/** Idempotent reaction document with deterministic compound identity encoded in `_id`. */
@Document("comment_reaction")
public record CommentReactionDocument(
        @Id String id,
        String threadId,
        String userId,
        ReactionType type,
        LocalDateTime createdAt) {}
