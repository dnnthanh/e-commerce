package com.dnnthanh.marketplace.be.order.api.adapter.out.persistence.repository;

import com.dnnthanh.marketplace.be.order.api.adapter.out.persistence.entity.OrderInboxJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data repository for idempotent Order inbox receipts. */
public interface OrderInboxJpaRepository extends JpaRepository<OrderInboxJpaEntity, Long> {
    boolean existsByConsumerNameAndEventId(String consumerName, String eventId);
}
