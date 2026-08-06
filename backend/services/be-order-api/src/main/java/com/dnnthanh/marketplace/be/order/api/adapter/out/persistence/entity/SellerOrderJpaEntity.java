package com.dnnthanh.marketplace.be.order.api.adapter.out.persistence.entity;

import com.dnnthanh.marketplace.be.order.api.domain.enumtype.SellerOrderStatus;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** JPA persistence representation of one seller child order. */
@Entity
@Table(name = "seller_order")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SellerOrderJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Owning parent order. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderJpaEntity order;

    /** Seller identifier. */
    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    /** External child-order number. */
    @Column(name = "seller_order_no", nullable = false, unique = true, length = 64)
    private String sellerOrderNo;

    /** Gross amount snapshot. */
    @Column(name = "gross_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal grossAmount;

    /** Allocated discount snapshot. */
    @Column(name = "discount_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal discountAmount;

    /** Seller payable amount snapshot. */
    @Column(name = "payable_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal payableAmount;

    /** Seller child lifecycle status. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private SellerOrderStatus status;

    /** Line snapshots owned by this seller order. */
    @OneToMany(mappedBy = "sellerOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderLineJpaEntity> lines = new ArrayList<>();

    public SellerOrderJpaEntity(
            Long id,
            Long sellerId,
            String sellerOrderNo,
            BigDecimal grossAmount,
            BigDecimal discountAmount,
            BigDecimal payableAmount,
            SellerOrderStatus status) {
        this.id = id;
        this.sellerId = sellerId;
        this.sellerOrderNo = sellerOrderNo;
        this.grossAmount = grossAmount;
        this.discountAmount = discountAmount;
        this.payableAmount = payableAmount;
        this.status = status;
    }

    /** Attaches the child to its aggregate root. */
    public void attachTo(OrderJpaEntity parent) {
        this.order = parent;
    }

    /** Replaces line children while maintaining the back-reference required by JPA. */
    public void replaceLines(List<OrderLineJpaEntity> children) {
        lines.clear();
        children.forEach(
                child -> {
                    child.attachTo(this);
                    lines.add(child);
                });
    }
}
