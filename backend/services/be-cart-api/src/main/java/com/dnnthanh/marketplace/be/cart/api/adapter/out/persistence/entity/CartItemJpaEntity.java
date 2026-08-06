package com.dnnthanh.marketplace.be.cart.api.adapter.out.persistence.entity;

import com.dnnthanh.marketplace.be.cart.api.domain.model.ShoppingCart.ItemState;
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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** JPA child entity for a cart line. */
@Entity
@Table(name = "cart_item")
@Getter
@Setter
@NoArgsConstructor()
public class CartItemJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cart_id", nullable = false)
    private CartJpaEntity cart;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "sku_id", nullable = false)
    private Long skuId;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "price_snapshot", nullable = false, precision = 19, scale = 2)
    private BigDecimal priceSnapshot;

    @Column(nullable = false)
    private boolean selected;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_state", nullable = false, length = 32)
    private ItemState itemState;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public void attachTo(CartJpaEntity value) {
        this.cart = value;
        this.updatedAt = LocalDateTime.now();
    }
}
