package com.dnnthanh.marketplace.be.pricing.api.adapter.out.persistence.repository;

import com.dnnthanh.marketplace.be.pricing.api.adapter.out.persistence.entity.PriceRuleHistoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data repository for append-only price history. */
public interface PriceRuleHistoryJpaRepository
        extends JpaRepository<PriceRuleHistoryJpaEntity, Long> {}
