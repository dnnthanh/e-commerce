package com.dnnthanh.marketplace.be.payment.api.adapter.out.persistence.repository;

import com.dnnthanh.marketplace.be.payment.api.adapter.out.persistence.entity.PaymentQueryJpaEntity;
import com.dnnthanh.marketplace.be.payment.api.adapter.out.persistence.projection.PaymentSearchProjection;
import com.dnnthanh.marketplace.be.payment.api.application.query.PaymentSearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

/** Native relational payment search using grouped business filters plus Pageable. */
public interface PaymentQueryJpaRepository extends Repository<PaymentQueryJpaEntity, Long> {
    @Query(
            value =
                    """
      SELECT payment_key AS paymentKey, order_id AS orderId, user_id AS userId, provider, amount,
             status, updated_at AS updatedAt
      FROM payment
      WHERE (:#{#criteria.statusValue()} IS NULL OR status = :#{#criteria.statusValue()})
      ORDER BY id DESC
      """,
            countQuery =
                    """
      SELECT COUNT(*)
      FROM payment
      WHERE (:#{#criteria.statusValue()} IS NULL OR status = :#{#criteria.statusValue()})
      """,
            nativeQuery = true)
    Page<PaymentSearchProjection> search(
            @Param("criteria") PaymentSearchCriteria criteria, Pageable pageable);
}
