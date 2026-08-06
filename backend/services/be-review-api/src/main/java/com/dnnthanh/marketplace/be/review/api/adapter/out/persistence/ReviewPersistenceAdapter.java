package com.dnnthanh.marketplace.be.review.api.adapter.out.persistence;

import com.dnnthanh.marketplace.be.platform.stereotype.Persistence;
import com.dnnthanh.marketplace.be.review.api.adapter.out.persistence.entity.ProductReviewJpaEntity;
import com.dnnthanh.marketplace.be.review.api.adapter.out.persistence.exception.ReviewPersistenceException;
import com.dnnthanh.marketplace.be.review.api.adapter.out.persistence.mapper.ProductReviewPersistenceMapper;
import com.dnnthanh.marketplace.be.review.api.adapter.out.persistence.repository.ProductReviewJpaRepository;
import com.dnnthanh.marketplace.be.review.api.adapter.out.persistence.repository.VerifiedPurchaseJpaRepository;
import com.dnnthanh.marketplace.be.review.api.application.port.out.ProductReviewPort;
import com.dnnthanh.marketplace.be.review.api.application.port.out.ReviewQueryPort;
import com.dnnthanh.marketplace.be.review.api.application.query.ReviewSearchCriteria;
import com.dnnthanh.marketplace.be.review.api.application.query.ReviewSummaryQueryResult;
import com.dnnthanh.marketplace.be.review.api.domain.model.ProductReview;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.annotation.Transactional;

/**
 * Review persistence adapter.
 *
 * <p>Ordinary aggregate CRUD is Spring Data JPA. Public search/summary is native SQL through Spring
 * Data. JdbcClient is retained only for the vendor-specific JSON transactional-outbox insert.
 */
@Persistence
@RequiredArgsConstructor
public class ReviewPersistenceAdapter implements ProductReviewPort, ReviewQueryPort {
    private static final String REVIEW_CHANGED = "REVIEW_CHANGED";

    private final ProductReviewJpaRepository reviews;
    private final VerifiedPurchaseJpaRepository verifiedPurchases;
    private final ProductReviewPersistenceMapper mapper;
    private final JdbcClient jdbc;

    @Override
    @Transactional(readOnly = true)
    public Optional<ProductReview> findById(Long reviewId) {
        return reviews.findById(reviewId).map(mapper::toDomain);
    }

    @Override
    @Transactional
    public ProductReview save(ProductReview review) {
        ProductReviewJpaEntity entity =
                review.reviewId() == null
                        ? mapper.newEntity(review)
                        : reviews.findById(review.reviewId())
                                .orElseThrow(
                                        () ->
                                                new ReviewPersistenceException(
                                                        "Review does not exist: "
                                                                + review.reviewId()));
        if (review.reviewId() != null) {
            mapper.copy(review, entity);
        }
        ProductReviewJpaEntity saved = reviews.saveAndFlush(entity);
        emitOutbox(saved);
        return mapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsActiveReview(String userId, Long orderLineId) {
        return reviews.existsActiveReview(userId, orderLineId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Long> verifiedProduct(String userId, Long orderLineId) {
        return verifiedPurchases.findVerifiedProduct(userId, orderLineId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductReview> findPublished(ReviewSearchCriteria criteria, Pageable pageable) {
        return reviews.findPublishedNative(criteria, pageable).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewSummaryQueryResult summary(Long productId) {
        var value = reviews.summarizeNative(productId);
        return new ReviewSummaryQueryResult(value.getAverageRating(), value.getReviewCount());
    }

    private void emitOutbox(ProductReviewJpaEntity review) {
        int changed =
                jdbc.sql(
                                """
                INSERT INTO outbox_event(
                    event_id,aggregate_id,event_type,payload_json,status,created_at)
                VALUES(
                    :eventId,:aggregateId,:eventType,
                    JSON_OBJECT('productId',:productId,'reviewId',:reviewId,'status',:status),
                    'PENDING',NOW())
                """)
                        .param("eventId", UUID.randomUUID().toString())
                        .param("aggregateId", review.getProductId().toString())
                        .param("eventType", REVIEW_CHANGED)
                        .param("productId", review.getProductId())
                        .param("reviewId", review.getId())
                        .param("status", review.getStatus())
                        .update();
        if (changed != 1) {
            throw new ReviewPersistenceException("Review outbox event was not persisted");
        }
    }
}
