package com.dnnthanh.marketplace.be.review.api.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Materialized verified-purchase fact consumed by review eligibility. */
@Entity
@Table(name = "verified_purchase")
@Getter
@NoArgsConstructor
public class VerifiedPurchaseJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, length = 64)
    private String userId;

    @Column(name = "order_line_id", nullable = false, unique = true)
    private Long orderLineId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "sku_id", nullable = false)
    private Long skuId;

    @Column(name = "delivered_at", nullable = false)
    private LocalDateTime deliveredAt;
}
