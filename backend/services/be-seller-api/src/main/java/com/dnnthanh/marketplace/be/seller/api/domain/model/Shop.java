package com.dnnthanh.marketplace.be.seller.api.domain.model;

import com.dnnthanh.marketplace.be.seller.api.domain.enumtype.SellerStatus;
import com.dnnthanh.marketplace.be.seller.api.domain.exception.InvalidShopException;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.Getter;

/** Seller-owned public shop aggregate. */
@Getter
public final class Shop {
    private final Long id;
    private final Long sellerId;
    private final String slug;
    private final SellerStatus status;
    private String name;
    private String description;
    private LocalDateTime updatedAt;

    /** Rehydrates a persisted shop. Input normalization is handled by the API boundary. */
    public Shop(
            Long id,
            Long sellerId,
            String slug,
            String name,
            String description,
            SellerStatus status,
            LocalDateTime updatedAt) {
        this.id = id;
        this.sellerId = Objects.requireNonNull(sellerId);
        this.slug = Objects.requireNonNull(slug);
        this.name = requireName(name);
        this.description = description;
        this.status = Objects.requireNonNull(status);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    /** Updates material public information only when it actually changed. */
    public boolean updateMaterialInfo(String newName, String newDescription) {
        String validatedName = requireName(newName);
        boolean changed =
                !validatedName.equals(name) || !Objects.equals(newDescription, description);
        if (changed) {
            name = validatedName;
            description = newDescription;
            updatedAt = LocalDateTime.now();
        }
        return changed;
    }

    private static String requireName(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidShopException("shop name is required");
        }
        return value;
    }

    public Long id() {
        return id;
    }

    public Long sellerId() {
        return sellerId;
    }

    public String slug() {
        return slug;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public SellerStatus status() {
        return status;
    }

    public LocalDateTime updatedAt() {
        return updatedAt;
    }
}
