package com.dnnthanh.marketplace.be.settlement.api.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Oracle settlement period persistence entity. */
@Entity
@Table(name = "settlement")
@Getter
@Setter
@NoArgsConstructor
public class SettlementJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "settlement_no", nullable = false, unique = true, length = 64)
    private String settlementNo;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "period_start", nullable = false)
    private LocalDateTime periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDateTime periodEnd;

    @Column(name = "gross_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal grossAmount;

    @Column(name = "commission_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal commissionAmount;

    @Column(name = "payable_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal payableAmount;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @Column(name = "hold_reason", length = 512)
    private String holdReason;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
