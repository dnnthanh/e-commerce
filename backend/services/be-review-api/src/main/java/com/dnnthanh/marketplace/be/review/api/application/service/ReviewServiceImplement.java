package com.dnnthanh.marketplace.be.review.api.application.service;

import com.dnnthanh.marketplace.be.platform.context.UserContext;
import com.dnnthanh.marketplace.be.platform.stereotype.UseCase;
import com.dnnthanh.marketplace.be.review.api.application.command.CreateReviewCommand;
import com.dnnthanh.marketplace.be.review.api.application.command.EditReviewCommand;
import com.dnnthanh.marketplace.be.review.api.application.dto.ReviewSummaryResult;
import com.dnnthanh.marketplace.be.review.api.application.port.in.ReviewUseCase;
import com.dnnthanh.marketplace.be.review.api.application.port.out.ProductReviewPort;
import com.dnnthanh.marketplace.be.review.api.application.port.out.ReviewQueryPort;
import com.dnnthanh.marketplace.be.review.api.application.query.ReviewSearchCriteria;
import com.dnnthanh.marketplace.be.review.api.domain.exception.InvalidReviewException;
import com.dnnthanh.marketplace.be.review.api.domain.exception.ReviewPermissionException;
import com.dnnthanh.marketplace.be.review.api.domain.model.ProductReview;
import java.time.Duration;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Canonical customer review lifecycle: verified purchase, edit window, delete and reactions. */
@UseCase
@RequiredArgsConstructor
public class ReviewServiceImplement implements ReviewUseCase {
    private static final Duration EDIT_WINDOW = Duration.ofDays(7);
    private final ProductReviewPort reviews;
    private final ReviewQueryPort queries;
    private final UserContext user;

    @Override
    public Page<ProductReview> list(ReviewSearchCriteria criteria, Pageable pageable) {
        return queries.findPublished(criteria, pageable);
    }

    @Override
    public ReviewSummaryResult summary(Long productId) {
        var summary = queries.summary(productId);
        return new ReviewSummaryResult(productId, summary.averageRating(), summary.reviewCount());
    }

    @Override
    public ProductReview create(CreateReviewCommand command) {
        if (reviews.existsActiveReview(user.userId(), command.orderLineId())) {
            throw new InvalidReviewException("An active review already exists for this order line");
        }
        Long productId =
                reviews.verifiedProduct(user.userId(), command.orderLineId())
                        .orElseThrow(
                                () ->
                                        new ReviewPermissionException(
                                                "Only verified purchasers can review"));
        ProductReview review =
                new ProductReview(
                        null,
                        user.userId(),
                        productId,
                        command.orderLineId(),
                        command.rating(),
                        command.title(),
                        command.content(),
                        LocalDateTime.now());
        return reviews.save(review);
    }

    @Override
    public ProductReview edit(Long reviewId, EditReviewCommand command) {
        ProductReview review = load(reviewId);
        review.edit(
                user.userId(),
                command.rating(),
                command.title(),
                command.content(),
                LocalDateTime.now(),
                EDIT_WINDOW);
        return reviews.save(review);
    }

    @Override
    public void delete(Long reviewId) {
        ProductReview review = load(reviewId);
        review.delete(user.userId());
        reviews.save(review);
    }

    @Override
    public ProductReview markHelpful(Long reviewId, boolean active) {
        ProductReview review = load(reviewId);
        if (active) {
            review.markHelpful(user.userId());
        } else {
            review.removeHelpful(user.userId());
        }
        return reviews.save(review);
    }

    private ProductReview load(Long id) {
        return reviews.findById(id)
                .orElseThrow(() -> new InvalidReviewException("Review not found: " + id));
    }
}
