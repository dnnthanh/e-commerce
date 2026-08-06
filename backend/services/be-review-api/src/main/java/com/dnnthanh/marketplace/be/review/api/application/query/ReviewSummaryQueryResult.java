package com.dnnthanh.marketplace.be.review.api.application.query;

/** Review summary returned by the application query. */
public record ReviewSummaryQueryResult(double averageRating, long reviewCount) {}
