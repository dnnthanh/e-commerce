package com.dnnthanh.marketplace.be.order.api.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** JPA order-line persistence snapshot. */
@Entity
@Table(name = "order_line")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderLineJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Owning seller child order. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seller_order_id", nullable = false)
    private SellerOrderJpaEntity sellerOrder;

    /** SKU identifier. */
    @Column(name = "sku_id", nullable = false)
    private Long skuId;

    /** Purchased quantity. */
    @Column(nullable = false)
    private int quantity;

    /** Repriced unit-price snapshot. */
    @Column(name = "unit_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal unitPrice;

    /** Allocated order discount. */
    @Column(name = "allocated_discount", nullable = false, precision = 19, scale = 2)
    private BigDecimal allocatedDiscount;

    /** Refundable line net amount. */
    @Column(name = "net_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal netAmount;

    public OrderLineJpaEntity(
            Long id,
            Long skuId,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal allocatedDiscount,
            BigDecimal netAmount) {
        this.id = id;
        this.skuId = skuId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.allocatedDiscount = allocatedDiscount;
        this.netAmount = netAmount;
    }

    /** Attaches this line to its seller order. */
    public void attachTo(SellerOrderJpaEntity parent) {
        this.sellerOrder = parent;
    }
}
