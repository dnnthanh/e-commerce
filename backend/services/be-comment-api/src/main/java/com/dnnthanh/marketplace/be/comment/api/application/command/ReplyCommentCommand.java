package com.dnnthanh.marketplace.be.comment.api.application.command;

/** Application command for replying to a comment thread. */
public record ReplyCommentCommand(String threadId, String content) {}
