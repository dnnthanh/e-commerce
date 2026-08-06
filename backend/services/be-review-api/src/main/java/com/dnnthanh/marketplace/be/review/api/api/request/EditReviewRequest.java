package com.dnnthanh.marketplace.be.review.api.api.request;

/** Request used to edit a review. */
public record EditReviewRequest(int rating, String title, String content) {}
