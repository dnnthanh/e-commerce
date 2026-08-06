package com.dnnthanh.marketplace.be.catalog.api.domain.model;

import com.dnnthanh.marketplace.be.catalog.api.domain.exception.InvalidProductException;
import com.dnnthanh.marketplace.be.catalog.api.domain.exception.ProductStateConflictException;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.Getter;

/** Rich product aggregate that owns publication invariants. */
@Getter
public final class Product {
    private Long id;
    private final Long sellerId;
    private final Long categoryId;
    private String name;
    private String description;
    private ProductStatus status;
    private long version;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Product(Long sellerId, Long categoryId, String name, String description) {
        this(
                null,
                sellerId,
                categoryId,
                name,
                description,
                ProductStatus.DRAFT,
                0L,
                LocalDateTime.now(),
                LocalDateTime.now());
    }

    private Product(
            Long id,
            Long sellerId,
            Long categoryId,
            String name,
            String description,
            ProductStatus status,
            long version,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        if (sellerId == null || categoryId == null || name == null || name.isBlank()) {
            throw new InvalidProductException("sellerId, categoryId and name are required");
        }
        this.id = id;
        this.sellerId = sellerId;
        this.categoryId = categoryId;
        this.name = name;
        this.description = description;
        this.status = Objects.requireNonNull(status);
        this.version = version;
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    /** Rehydrates a persisted product without changing audit timestamps. */
    public static Product rehydrate(
            Long id,
            Long sellerId,
            Long categoryId,
            String name,
            String description,
            ProductStatus status,
            long version,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        return new Product(
                id, sellerId, categoryId, name, description, status, version, createdAt, updatedAt);
    }

    public void publish(boolean mediaReady) {
        if (status != ProductStatus.DRAFT) {
            throw new ProductStateConflictException("Only a draft product can be published");
        }
        if (!mediaReady) {
            throw new ProductStateConflictException("Required product media is not ready");
        }
        status = ProductStatus.PUBLISHED;
        updatedAt = LocalDateTime.now();
    }

    public void updateContent(String newName, String newDescription) {
        if (status == ProductStatus.ARCHIVED) {
            throw new ProductStateConflictException("Archived product cannot be edited");
        }
        if (newName == null || newName.isBlank()) {
            throw new InvalidProductException("name is required");
        }
        name = newName;
        description = newDescription;
        updatedAt = LocalDateTime.now();
    }

    public Long id() {
        return id;
    }

    public Long sellerId() {
        return sellerId;
    }

    public Long categoryId() {
        return categoryId;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public ProductStatus status() {
        return status;
    }

    public long version() {
        return version;
    }

    public LocalDateTime createdAt() {
        return createdAt;
    }

    public LocalDateTime updatedAt() {
        return updatedAt;
    }
}
