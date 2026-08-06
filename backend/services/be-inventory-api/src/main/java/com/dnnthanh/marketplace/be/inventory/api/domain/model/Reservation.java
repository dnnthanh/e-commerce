package com.dnnthanh.marketplace.be.inventory.api.domain.model;

import com.dnnthanh.marketplace.be.inventory.api.domain.enumtype.ReservationStatus;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Immutable reservation snapshot.
 *
 * @param reservationKey idempotency key
 * @param skuId SKU identifier
 * @param warehouseId warehouse identifier
 * @param quantity reserved quantity
 * @param status lifecycle status
 * @param expiresAt expiration timestamp
 */
public record Reservation(
        String reservationKey,
        Long skuId,
        Long warehouseId,
        long quantity,
        ReservationStatus status,
        LocalDateTime expiresAt) {

    public Reservation {
        Objects.requireNonNull(reservationKey, "reservationKey");
        Objects.requireNonNull(skuId, "skuId");
        Objects.requireNonNull(warehouseId, "warehouseId");
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(expiresAt, "expiresAt");
        if (quantity <= 0) {
            throw new com.dnnthanh.marketplace.be.inventory.api.domain.exception
                    .InvalidInventoryMutationException("Reservation quantity must be positive");
        }
    }
}
