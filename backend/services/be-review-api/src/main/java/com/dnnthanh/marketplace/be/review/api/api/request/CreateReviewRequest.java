package com.dnnthanh.marketplace.be.review.api.api.request;

/** Request used to create a review. */
public record CreateReviewRequest(Long orderLineId, int rating, String title, String content) {}
