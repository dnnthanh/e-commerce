package com.dnnthanh.marketplace.be.catalog.api.adapter.out.persistence.entity;

import com.dnnthanh.marketplace.be.catalog.api.domain.model.Product;
import com.dnnthanh.marketplace.be.catalog.api.domain.model.ProductStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Relational persistence model for the Catalog Product aggregate. */
@Entity
@Table(name = "product")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ProductStatus status;

    @Version
    @Column(nullable = false)
    private long version;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static ProductJpaEntity fromNew(Product product) {
        ProductJpaEntity entity = new ProductJpaEntity();
        entity.sellerId = product.sellerId();
        entity.categoryId = product.categoryId();
        entity.name = product.name();
        entity.description = product.description();
        entity.status = product.status();
        entity.createdAt = product.createdAt();
        entity.updatedAt = product.updatedAt();
        return entity;
    }

    /** Applies mutable aggregate fields while leaving id/version under JPA control. */
    public void apply(Product product) {
        this.name = product.name();
        this.description = product.description();
        this.status = product.status();
        this.updatedAt = product.updatedAt();
    }

    /** Converts persistence state back to the domain aggregate. */
    public Product toDomain() {
        return Product.rehydrate(
                id, sellerId, categoryId, name, description, status, version, createdAt, updatedAt);
    }
}
