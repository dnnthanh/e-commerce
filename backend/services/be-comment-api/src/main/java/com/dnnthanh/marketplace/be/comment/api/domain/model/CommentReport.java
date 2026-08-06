package com.dnnthanh.marketplace.be.comment.api.domain.model;

import com.dnnthanh.marketplace.be.comment.api.domain.enumtype.CommentReportReason;
import java.time.LocalDateTime;
import java.util.UUID;

/** Moderation report submitted by a user for one thread. */
public record CommentReport(
        String id,
        String threadId,
        String reporterUserId,
        CommentReportReason reason,
        String details,
        LocalDateTime createdAt) {
    public static CommentReport create(
            String threadId,
            String reporterUserId,
            CommentReportReason reason,
            String details,
            LocalDateTime now) {
        return new CommentReport(
                UUID.randomUUID().toString(), threadId, reporterUserId, reason, details, now);
    }
}
