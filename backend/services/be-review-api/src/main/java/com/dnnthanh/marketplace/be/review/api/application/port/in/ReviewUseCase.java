package com.dnnthanh.marketplace.be.review.api.application.port.in;

import com.dnnthanh.marketplace.be.review.api.application.command.CreateReviewCommand;
import com.dnnthanh.marketplace.be.review.api.application.command.EditReviewCommand;
import com.dnnthanh.marketplace.be.review.api.application.dto.ReviewSummaryResult;
import com.dnnthanh.marketplace.be.review.api.application.query.ReviewSearchCriteria;
import com.dnnthanh.marketplace.be.review.api.domain.model.ProductReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Inbound application port for the verified-purchase review lifecycle. */
public interface ReviewUseCase {
    Page<ProductReview> list(ReviewSearchCriteria criteria, Pageable pageable);

    ReviewSummaryResult summary(Long productId);

    ProductReview create(CreateReviewCommand command);

    ProductReview edit(Long reviewId, EditReviewCommand command);

    void delete(Long reviewId);

    ProductReview markHelpful(Long reviewId, boolean active);
}
