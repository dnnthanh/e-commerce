package com.dnnthanh.marketplace.be.inventory.api.domain.model;

import com.dnnthanh.marketplace.be.inventory.api.domain.exception.InsufficientStockException;
import com.dnnthanh.marketplace.be.inventory.api.domain.exception.InvalidInventoryMutationException;
import java.util.Objects;

/** Inventory aggregate enforcing sellable-stock invariants. */
public final class InventoryBalance {

    private final Long skuId;

    private final Long warehouseId;

    private final long onHand;

    private long reserved;

    private final long version;

    /**
     * Rehydrates a persisted balance.
     *
     * @param skuId SKU identifier
     * @param warehouseId warehouse identifier
     * @param onHand physical quantity
     * @param reserved reserved quantity
     * @param version optimistic-lock version
     */
    public InventoryBalance(
            Long skuId, Long warehouseId, long onHand, long reserved, long version) {
        this.skuId = Objects.requireNonNull(skuId, "skuId");
        this.warehouseId = Objects.requireNonNull(warehouseId, "warehouseId");
        if (onHand < 0 || reserved < 0 || reserved > onHand) {
            throw new InvalidInventoryMutationException("Invalid persisted inventory quantities");
        }
        this.onHand = onHand;
        this.reserved = reserved;
        this.version = version;
    }

    /**
     * Reserves quantity while preventing oversell.
     *
     * @param quantity requested quantity
     */
    public void reserve(long quantity) {
        if (quantity <= 0) {
            throw new InvalidInventoryMutationException("Reservation quantity must be positive");
        }
        if (available() < quantity) {
            throw new InsufficientStockException(
                    skuId.toString(), warehouseId, quantity, available());
        }
        reserved += quantity;
    }

    /**
     * Releases quantity from the reserved bucket.
     *
     * @param quantity quantity to release
     */
    public void release(long quantity) {
        if (quantity <= 0 || quantity > reserved) {
            throw new InvalidInventoryMutationException(
                    "Release quantity exceeds active reservation");
        }
        reserved -= quantity;
    }

    /**
     * @return sellable quantity
     */
    public long available() {
        return onHand - reserved;
    }

    /**
     * @return SKU identifier
     */
    public Long skuId() {
        return skuId;
    }

    /**
     * @return warehouse identifier
     */
    public Long warehouseId() {
        return warehouseId;
    }

    /**
     * @return physical quantity
     */
    public long onHand() {
        return onHand;
    }

    /**
     * @return reserved quantity
     */
    public long reserved() {
        return reserved;
    }

    /**
     * @return optimistic-lock version
     */
    public long version() {
        return version;
    }
}
