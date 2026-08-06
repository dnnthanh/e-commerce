package com.dnnthanh.marketplace.be.review.api.application.dto;

/** Review aggregate summary returned by the application boundary. */
public record ReviewSummaryResult(Long productId, double averageRating, long reviewCount) {}
