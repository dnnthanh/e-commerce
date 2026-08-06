package com.dnnthanh.marketplace.be.review.api.application.port.out;

import com.dnnthanh.marketplace.be.review.api.domain.model.ProductReview;
import java.util.Optional;

/** Canonical review aggregate persistence plus verified-purchase eligibility boundary. */
public interface ProductReviewPort {
    Optional<ProductReview> findById(Long reviewId);

    ProductReview save(ProductReview review);

    boolean existsActiveReview(String userId, Long orderLineId);

    Optional<Long> verifiedProduct(String userId, Long orderLineId);
}
