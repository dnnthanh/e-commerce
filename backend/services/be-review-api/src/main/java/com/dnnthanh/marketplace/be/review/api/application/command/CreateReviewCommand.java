package com.dnnthanh.marketplace.be.review.api.application.command;

/** Application command for creating a verified-purchase review. */
public record CreateReviewCommand(Long orderLineId, int rating, String title, String content) {}
