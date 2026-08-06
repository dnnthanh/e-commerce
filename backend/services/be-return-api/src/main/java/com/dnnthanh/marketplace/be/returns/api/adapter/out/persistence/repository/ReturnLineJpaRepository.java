package com.dnnthanh.marketplace.be.returns.api.adapter.out.persistence.repository;

import com.dnnthanh.marketplace.be.returns.api.adapter.out.persistence.entity.ReturnLineJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Spring Data repository for seller ownership and cumulative return quantity checks. */
public interface ReturnLineJpaRepository extends JpaRepository<ReturnLineJpaEntity, Long> {
    boolean existsByRequest_ReturnKeyAndSellerId(String returnKey, Long sellerId);

    @Query(
            """
            select coalesce(sum(line.quantity), 0)
            from ReturnLineJpaEntity line
            where line.request.orderId = :orderId
              and line.orderLineId = :orderLineId
              and line.request.status not in (
                com.dnnthanh.marketplace.be.returns.api.domain.enumtype.ReturnStatus.REJECTED,
                com.dnnthanh.marketplace.be.returns.api.domain.enumtype.ReturnStatus.CLOSED)
            """)
    long activeReturnedQuantity(
            @Param("orderId") String orderId, @Param("orderLineId") Long orderLineId);
}
