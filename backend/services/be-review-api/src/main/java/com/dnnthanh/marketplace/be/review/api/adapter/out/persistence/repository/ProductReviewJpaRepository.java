package com.dnnthanh.marketplace.be.review.api.adapter.out.persistence.repository;

import com.dnnthanh.marketplace.be.review.api.adapter.out.persistence.entity.ProductReviewJpaEntity;
import com.dnnthanh.marketplace.be.review.api.application.query.ReviewSearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Spring Data repository; search/read-model queries intentionally use native SQL. */
public interface ProductReviewJpaRepository extends JpaRepository<ProductReviewJpaEntity, Long> {

    @Query(
            value =
                    """
          SELECT CASE WHEN COUNT(*) > 0 THEN TRUE ELSE FALSE END
          FROM review
          WHERE user_id = :userId
            AND order_line_id = :orderLineId
            AND status <> 'DELETED'
          """,
            nativeQuery = true)
    boolean existsActiveReview(
            @Param("userId") String userId, @Param("orderLineId") Long orderLineId);

    @Query(
            value =
                    """
          SELECT *
          FROM review
          WHERE product_id = :#{#criteria.productId}
            AND status = 'PUBLISHED'
          ORDER BY created_at DESC, id DESC
          """,
            countQuery =
                    """
          SELECT COUNT(*)
          FROM review
          WHERE product_id = :#{#criteria.productId}
            AND status = 'PUBLISHED'
          """,
            nativeQuery = true)
    Page<ProductReviewJpaEntity> findPublishedNative(
            @Param("criteria") ReviewSearchCriteria criteria, Pageable pageable);

    @Query(
            value =
                    """
          SELECT COALESCE(AVG(rating), 0) AS averageRating,
                 COUNT(*) AS reviewCount
          FROM review
          WHERE product_id = :productId
            AND status = 'PUBLISHED'
          """,
            nativeQuery = true)
    ReviewSummaryProjection summarizeNative(@Param("productId") Long productId);

    interface ReviewSummaryProjection {
        double getAverageRating();

        long getReviewCount();
    }
}
