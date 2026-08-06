package com.dnnthanh.marketplace.be.order.api.adapter.out.persistence.repository;

import com.dnnthanh.marketplace.be.order.api.adapter.out.persistence.entity.OrderJpaEntity;
import com.dnnthanh.marketplace.be.order.api.application.query.OrderSearchCriteria;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Spring Data repository for Order CRUD plus explicit native search. */
public interface OrderJpaRepository extends JpaRepository<OrderJpaEntity, Long> {
    @EntityGraph(attributePaths = {"sellerOrders", "sellerOrders.lines"})
    Optional<OrderJpaEntity> findByOrderNo(String orderNo);

    @EntityGraph(attributePaths = {"sellerOrders", "sellerOrders.lines"})
    Optional<OrderJpaEntity> findByCheckoutKey(String checkoutKey);

    @EntityGraph(attributePaths = {"sellerOrders", "sellerOrders.lines"})
    @Query(
            value =
                    """
                    SELECT *
                    FROM marketplace_order
                    WHERE status IN (:statuses)
                      AND created_at < :cutoff
                    ORDER BY created_at, id
                    """,
            nativeQuery = true)
    List<OrderJpaEntity> findExpirableNative(
            @Param("statuses") Collection<String> statuses,
            @Param("cutoff") LocalDateTime cutoff,
            Pageable pageable);

    /**
     * Native SQL owns filtering/pagination; a second graph fetch avoids N+1 on seller orders/lines.
     */
    @Query(
            value =
                    """
                    SELECT mo.id
                    FROM marketplace_order mo
                    WHERE (:#{#criteria.userId} IS NULL OR :#{#criteria.userId} = '' OR mo.user_id = :#{#criteria.userId})
                      AND (:#{#criteria.statusValue()} IS NULL OR :#{#criteria.statusValue()} = '' OR mo.status = :#{#criteria.statusValue()})
                    ORDER BY mo.created_at DESC, mo.id DESC
                    """,
            countQuery =
                    """
                    SELECT COUNT(*)
                    FROM marketplace_order mo
                    WHERE (:#{#criteria.userId} IS NULL OR :#{#criteria.userId} = '' OR mo.user_id = :#{#criteria.userId})
                      AND (:#{#criteria.statusValue()} IS NULL OR :#{#criteria.statusValue()} = '' OR mo.status = :#{#criteria.statusValue()})
                    """,
            nativeQuery = true)
    Page<Long> searchIdsNative(@Param("criteria") OrderSearchCriteria criteria, Pageable pageable);

    @EntityGraph(attributePaths = {"sellerOrders", "sellerOrders.lines"})
    @Query("select distinct o from OrderJpaEntity o where o.id in :ids")
    List<OrderJpaEntity> findAggregateGraphByIdIn(@Param("ids") Collection<Long> ids);
}
