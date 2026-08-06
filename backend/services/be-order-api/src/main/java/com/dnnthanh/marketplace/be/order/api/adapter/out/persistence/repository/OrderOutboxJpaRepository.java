package com.dnnthanh.marketplace.be.order.api.adapter.out.persistence.repository;

import com.dnnthanh.marketplace.be.order.api.adapter.out.persistence.entity.OrderOutboxJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data repository for Order outbox records. */
public interface OrderOutboxJpaRepository extends JpaRepository<OrderOutboxJpaEntity, Long> {}
