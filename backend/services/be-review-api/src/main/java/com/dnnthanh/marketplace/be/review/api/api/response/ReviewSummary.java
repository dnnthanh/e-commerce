package com.dnnthanh.marketplace.be.review.api.api.response;

/** Aggregated review summary for a product. */
public record ReviewSummary(Long productId, double averageRating, long reviewCount) {}
