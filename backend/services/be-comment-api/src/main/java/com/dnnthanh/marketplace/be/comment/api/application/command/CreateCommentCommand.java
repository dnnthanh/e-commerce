package com.dnnthanh.marketplace.be.comment.api.application.command;

/** Application command for creating a product comment thread. */
public record CreateCommentCommand(Long productId, Long sellerId, String content) {}
