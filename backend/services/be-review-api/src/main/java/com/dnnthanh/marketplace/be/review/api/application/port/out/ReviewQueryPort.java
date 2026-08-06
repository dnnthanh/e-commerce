package com.dnnthanh.marketplace.be.review.api.application.port.out;

import com.dnnthanh.marketplace.be.review.api.application.query.ReviewSearchCriteria;
import com.dnnthanh.marketplace.be.review.api.application.query.ReviewSummaryQueryResult;
import com.dnnthanh.marketplace.be.review.api.domain.model.ProductReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Public review read-model boundary. */
public interface ReviewQueryPort {
    Page<ProductReview> findPublished(ReviewSearchCriteria criteria, Pageable pageable);

    ReviewSummaryQueryResult summary(Long productId);
}
