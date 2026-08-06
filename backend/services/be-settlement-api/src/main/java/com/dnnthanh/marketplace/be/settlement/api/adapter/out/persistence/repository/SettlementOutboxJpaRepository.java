package com.dnnthanh.marketplace.be.settlement.api.adapter.out.persistence.repository;

import com.dnnthanh.marketplace.be.settlement.api.adapter.out.persistence.entity.SettlementOutboxJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/** Ordinary JPA persistence for settlement transactional-outbox rows. */
public interface SettlementOutboxJpaRepository
        extends JpaRepository<SettlementOutboxJpaEntity, Long> {}
