package com.dnnthanh.marketplace.be.review.api.application.command;

/** Application command for editing a review. */
public record EditReviewCommand(int rating, String title, String content) {}
