package com.dnnthanh.marketplace.be.returns.api.adapter.out.persistence.entity;

import com.dnnthanh.marketplace.be.returns.api.domain.enumtype.InventoryDisposition;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** JPA representation of an immutable Order-line snapshot plus inspection result. */
@Entity
@Table(name = "return_line")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReturnLineJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "return_id", nullable = false)
    private ReturnRequestJpaEntity request;

    @Column(name = "order_line_id", nullable = false)
    private Long orderLineId;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "sku_id", nullable = false)
    private Long skuId;

    /** Requested quantity for this return. */
    @Column(nullable = false)
    private int quantity;

    /** Ordered quantity snapshot. */
    @Column(name = "ordered_quantity", nullable = false)
    private int orderedQuantity;

    /** Quantity already claimed by older active returns when this request was created. */
    @Column(name = "already_returned_quantity", nullable = false)
    private int alreadyReturnedQuantity;

    /** Unit refundable amount frozen from Order allocation. */
    @Column(name = "refundable_unit_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal refundableUnitAmount;

    /** Requested refundable amount kept for diagnostics/read models. */
    @Column(name = "refundable_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal refundableAmount;

    @Column(name = "delivered_at", nullable = false)
    private LocalDateTime deliveredAt;

    @Column(name = "accepted_quantity", nullable = false)
    private int acceptedQuantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "inventory_disposition", nullable = false, length = 32)
    private InventoryDisposition inventoryDisposition;

    public ReturnLineJpaEntity(
            Long orderLineId,
            Long sellerId,
            Long skuId,
            int quantity,
            int orderedQuantity,
            int alreadyReturnedQuantity,
            BigDecimal refundableUnitAmount,
            BigDecimal refundableAmount,
            LocalDateTime deliveredAt,
            int acceptedQuantity,
            InventoryDisposition inventoryDisposition) {
        this.orderLineId = orderLineId;
        this.sellerId = sellerId;
        this.skuId = skuId;
        this.quantity = quantity;
        this.orderedQuantity = orderedQuantity;
        this.alreadyReturnedQuantity = alreadyReturnedQuantity;
        this.refundableUnitAmount = refundableUnitAmount;
        this.refundableAmount = refundableAmount;
        this.deliveredAt = deliveredAt;
        this.acceptedQuantity = acceptedQuantity;
        this.inventoryDisposition = inventoryDisposition;
    }

    public void attachTo(ReturnRequestJpaEntity parent) {
        this.request = parent;
    }

    public void applyInspection(int acceptedQuantity, InventoryDisposition disposition) {
        this.acceptedQuantity = acceptedQuantity;
        this.inventoryDisposition = disposition;
    }
}
