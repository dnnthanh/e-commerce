package com.dnnthanh.marketplace.be.order.api.adapter.out.persistence.entity;

import com.dnnthanh.marketplace.be.order.api.domain.enumtype.CancellationReason;
import com.dnnthanh.marketplace.be.order.api.domain.enumtype.OrderStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** JPA persistence representation of the parent marketplace order. */
@Entity
@Table(name = "marketplace_order")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** External order number. */
    @Column(name = "order_no", nullable = false, unique = true, length = 64)
    private String orderNo;

    /** Checkout idempotency key. */
    @Column(name = "checkout_key", nullable = false, unique = true, length = 128)
    private String checkoutKey;

    /** Customer identifier. */
    @Column(name = "user_id", nullable = false, length = 64)
    private String userId;

    /** Gross amount snapshot. */
    @Column(name = "gross_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal grossAmount;

    /** Discount amount snapshot. */
    @Column(name = "discount_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal discountAmount;

    /** Payable amount snapshot. */
    @Column(name = "payable_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal payableAmount;

    /** Current order lifecycle status. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private OrderStatus status;

    /** Optional cancellation reason. */
    @Enumerated(EnumType.STRING)
    @Column(name = "cancellation_reason", length = 32)
    private CancellationReason cancellationReason;

    /** Creation timestamp. */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /** Last modification timestamp. */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /** Optimistic-lock version. */
    @Version
    @Column(nullable = false)
    private long version;

    /** Seller child entities owned by this aggregate. */
    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<SellerOrderJpaEntity> sellerOrders = new ArrayList<>();

    public OrderJpaEntity(
            Long id,
            String orderNo,
            String checkoutKey,
            String userId,
            BigDecimal grossAmount,
            BigDecimal discountAmount,
            BigDecimal payableAmount,
            OrderStatus status,
            CancellationReason cancellationReason,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            long version) {
        this.id = id;
        this.orderNo = orderNo;
        this.checkoutKey = checkoutKey;
        this.userId = userId;
        this.grossAmount = grossAmount;
        this.discountAmount = discountAmount;
        this.payableAmount = payableAmount;
        this.status = status;
        this.cancellationReason = cancellationReason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.version = version;
    }

    /** Synchronizes mutable aggregate state before persistence. */
    public void synchronize(
            String orderNo,
            String checkoutKey,
            String userId,
            BigDecimal grossAmount,
            BigDecimal discountAmount,
            BigDecimal payableAmount,
            OrderStatus status,
            CancellationReason cancellationReason,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        this.orderNo = orderNo;
        this.checkoutKey = checkoutKey;
        this.userId = userId;
        this.grossAmount = grossAmount;
        this.discountAmount = discountAmount;
        this.payableAmount = payableAmount;
        this.status = status;
        this.cancellationReason = cancellationReason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /** Replaces seller children while preserving aggregate ownership. */
    public void replaceSellerOrders(List<SellerOrderJpaEntity> children) {
        sellerOrders.clear();
        children.forEach(
                child -> {
                    child.attachTo(this);
                    sellerOrders.add(child);
                });
    }
}
