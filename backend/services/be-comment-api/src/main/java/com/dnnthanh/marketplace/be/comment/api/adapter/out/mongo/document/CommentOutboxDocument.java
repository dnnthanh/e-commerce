package com.dnnthanh.marketplace.be.comment.api.adapter.out.mongo.document;

import com.dnnthanh.marketplace.be.comment.api.domain.enumtype.CommentEventType;
import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/** Transactional Mongo outbox record for durable downstream notifications/integration events. */
@Document("comment_outbox")
public record CommentOutboxDocument(
        @Id String id,
        CommentEventType eventType,
        String aggregateId,
        String payloadJson,
        String status,
        LocalDateTime createdAt,
        LocalDateTime processedAt) {}
