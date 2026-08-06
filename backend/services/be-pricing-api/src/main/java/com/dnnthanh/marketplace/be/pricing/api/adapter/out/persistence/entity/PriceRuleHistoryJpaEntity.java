package com.dnnthanh.marketplace.be.pricing.api.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/** Append-only audit record for price-rule changes. */
@Entity
@Table(name = "price_rule_history")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PriceRuleHistoryJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "price_rule_id", nullable = false)
    private Long priceRuleId;

    @Column(nullable = false, length = 64)
    private String action;

    @Column(name = "actor_id", nullable = false, length = 64)
    private String actorId;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    public PriceRuleHistoryJpaEntity(
            Long priceRuleId, String action, String actorId, LocalDateTime changedAt) {
        this.priceRuleId = priceRuleId;
        this.action = action;
        this.actorId = actorId;
        this.changedAt = changedAt;
    }
}
