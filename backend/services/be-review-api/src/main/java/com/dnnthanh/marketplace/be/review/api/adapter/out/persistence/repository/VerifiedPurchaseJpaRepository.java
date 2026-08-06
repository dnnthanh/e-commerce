package com.dnnthanh.marketplace.be.review.api.adapter.out.persistence.repository;

import com.dnnthanh.marketplace.be.review.api.adapter.out.persistence.entity.VerifiedPurchaseJpaEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Verified-purchase read repository. */
public interface VerifiedPurchaseJpaRepository
        extends JpaRepository<VerifiedPurchaseJpaEntity, Long> {
    @Query(
            value =
                    """
          SELECT product_id
          FROM verified_purchase
          WHERE user_id = :userId
            AND order_line_id = :orderLineId
          """,
            nativeQuery = true)
    Optional<Long> findVerifiedProduct(
            @Param("userId") String userId, @Param("orderLineId") Long orderLineId);
}
