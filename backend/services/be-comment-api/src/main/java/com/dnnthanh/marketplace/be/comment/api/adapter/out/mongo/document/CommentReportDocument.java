package com.dnnthanh.marketplace.be.comment.api.adapter.out.mongo.document;

import com.dnnthanh.marketplace.be.comment.api.domain.enumtype.CommentReportReason;
import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/** Moderation report document. */
@Document("comment_report")
public record CommentReportDocument(
        @Id String id,
        String threadId,
        String reporterUserId,
        CommentReportReason reason,
        String details,
        LocalDateTime createdAt) {}
