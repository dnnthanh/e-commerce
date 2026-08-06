package com.dnnthanh.marketplace.be.comment.api.api.request;

import com.dnnthanh.marketplace.be.comment.api.domain.enumtype.CommentReportReason;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** User moderation report request. */
public record CommentReportRequest(
        @NotNull CommentReportReason reason, @Size(max = 1_000) String details) {}
