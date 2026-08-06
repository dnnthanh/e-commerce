package com.dnnthanh.marketplace.be.comment.api.application.command;

/** Application command for editing an owned comment thread. */
public record EditCommentCommand(String threadId, String content) {}
