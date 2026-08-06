package com.dnnthanh.marketplace.be.comment.api.api.response;

import com.dnnthanh.marketplace.be.comment.api.domain.enumtype.CommentReportReason;
import java.time.LocalDateTime;

/** Moderation report acknowledgement. */
public record CommentReportResponse(
        String id, String threadId, CommentReportReason reason, LocalDateTime createdAt) {}
