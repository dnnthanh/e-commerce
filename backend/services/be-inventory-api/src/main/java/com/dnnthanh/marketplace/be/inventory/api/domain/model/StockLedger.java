package com.dnnthanh.marketplace.be.inventory.api.domain.model;

import com.dnnthanh.marketplace.be.inventory.api.domain.exception.InsufficientStockException;
import com.dnnthanh.marketplace.be.inventory.api.domain.exception.InvalidInventoryMutationException;
import com.dnnthanh.marketplace.be.inventory.api.domain.exception.ReservationIdempotencyConflictException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/** In-memory domain representation of stock accounting used to protect balance invariants. */
public final class StockLedger {
    private final String sku;
    private final Long warehouseId;
    private long onHand;
    private long reserved;
    private long damaged;
    private long inTransit;
    private final Map<String, Long> activeReservations = new HashMap<>();

    public StockLedger(String sku, Long warehouseId, long onHand) {
        this.sku = Objects.requireNonNull(sku);
        this.warehouseId = Objects.requireNonNull(warehouseId);
        if (onHand < 0) throw new InvalidInventoryMutationException("On-hand cannot be negative");
        this.onHand = onHand;
    }

    /** Rehydrates persisted balance state without reconstructing historical reservation keys. */
    public static StockLedger rehydrate(String sku, Long warehouseId, long onHand, long reserved) {
        StockLedger ledger = new StockLedger(sku, warehouseId, onHand);
        if (reserved < 0 || reserved > onHand) {
            throw new InvalidInventoryMutationException("Persisted reserved stock is invalid");
        }
        ledger.reserved = reserved;
        return ledger;
    }

    /** Idempotently reserves stock by request key. */
    public synchronized boolean reserve(String requestKey, long quantity) {
        if (quantity <= 0)
            throw new InvalidInventoryMutationException("Reservation quantity must be positive");
        Long existing = activeReservations.get(requestKey);
        if (existing != null) {
            if (existing != quantity) throw new ReservationIdempotencyConflictException(requestKey);
            return false;
        }
        if (available() < quantity)
            throw new InsufficientStockException(sku, quantity, available());
        activeReservations.put(requestKey, quantity);
        reserved += quantity;
        return true;
    }

    public synchronized void confirm(String requestKey) {
        Long quantity = activeReservations.remove(requestKey);
        if (quantity == null) return;
        reserved -= quantity;
        onHand -= quantity;
    }

    public synchronized void release(String requestKey) {
        Long quantity = activeReservations.remove(requestKey);
        if (quantity == null) return;
        reserved -= quantity;
    }

    public synchronized void adjust(long delta) {
        if (onHand + delta < reserved)
            throw new InvalidInventoryMutationException(
                    "Adjustment would make reserved stock impossible");
        onHand += delta;
    }

    public long available() {
        return onHand - reserved - damaged;
    }

    public long onHand() {
        return onHand;
    }

    public long reserved() {
        return reserved;
    }

    public long damaged() {
        return damaged;
    }

    public long inTransit() {
        return inTransit;
    }

    public String sku() {
        return sku;
    }

    public Long warehouseId() {
        return warehouseId;
    }
}
