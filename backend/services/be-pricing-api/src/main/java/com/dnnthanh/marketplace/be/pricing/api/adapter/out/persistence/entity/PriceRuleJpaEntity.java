package com.dnnthanh.marketplace.be.pricing.api.adapter.out.persistence.entity;

import com.dnnthanh.marketplace.be.pricing.api.domain.enumtype.PriceSource;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** JPA read/write model for a temporal price rule. */
@Entity
@Table(name = "price_rule")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PriceRuleJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sku_id", nullable = false)
    private Long skuId;

    @Column(name = "seller_id")
    private Long sellerId;

    @Column(length = 32)
    private String channel;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "price_source", nullable = false, length = 32)
    private PriceSource priceSource;

    @Column(nullable = false)
    private int priority;

    @Column(name = "valid_from", nullable = false)
    private LocalDateTime validFrom;

    @Column(name = "valid_to")
    private LocalDateTime validTo;

    @Column(nullable = false, length = 32)
    private String status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
