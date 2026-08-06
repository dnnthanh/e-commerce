package com.dnnthanh.marketplace.be.cart.api.adapter.out.persistence.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** JPA source-of-truth entity for a versioned shopping cart. */
@Entity
@Table(name = "cart")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cart_key", nullable = false, unique = true, length = 128)
    private String cartKey;

    @Column(name = "user_id", length = 64)
    private String userId;

    @Column(nullable = false, length = 32)
    private String status;

    @Version
    @Column(nullable = false)
    private long version;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(
            mappedBy = "cart",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<CartItemJpaEntity> items = new ArrayList<>();

    public static CartJpaEntity newActive(String cartKey, String userId) {
        CartJpaEntity entity = new CartJpaEntity();
        entity.cartKey = cartKey;
        entity.userId = userId;
        entity.status = "ACTIVE";
        entity.updatedAt = LocalDateTime.now();
        return entity;
    }

    public void replaceItems(List<CartItemJpaEntity> values) {
        items.clear();
        values.forEach(
                item -> {
                    item.attachTo(this);
                    items.add(item);
                });
        updatedAt = LocalDateTime.now();
    }
}
