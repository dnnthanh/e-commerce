package com.dnnthanh.marketplace.be.comment.api.application.command;

import com.dnnthanh.marketplace.be.comment.api.domain.enumtype.CommentReportReason;

/** Application command for reporting a comment thread. */
public record ReportCommentCommand(String threadId, CommentReportReason reason, String details) {}
