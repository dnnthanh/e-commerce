package com.dnnthanh.marketplace.be.review.api.application.service;

import com.dnnthanh.marketplace.be.platform.stereotype.UseCase;
import com.dnnthanh.marketplace.be.review.api.application.port.out.ProductReviewPort;
import com.dnnthanh.marketplace.be.review.api.domain.exception.InvalidReviewException;
import com.dnnthanh.marketplace.be.review.api.domain.model.ProductReview;
import lombok.RequiredArgsConstructor;

/** Moderation commands remain separate from customer review mutation rules. */
@UseCase
@RequiredArgsConstructor
public class ReviewModerationService {
    private final ProductReviewPort repository;

    public ProductReview hide(Long reviewId) {
        ProductReview review = load(reviewId);
        review.hide();
        return repository.save(review);
    }

    public ProductReview restore(Long reviewId) {
        ProductReview review = load(reviewId);
        review.restore();
        return repository.save(review);
    }

    private ProductReview load(Long id) {
        return repository
                .findById(id)
                .orElseThrow(() -> new InvalidReviewException("Review not found: " + id));
    }
}
