package com.dnnthanh.marketplace.be.inventory.api.application.query;

import com.dnnthanh.marketplace.be.inventory.api.domain.enumtype.ReservationStatus;

/**
 * Reservation placement snapshot consumed by fulfillment.
 *
 * @param reservationKey idempotency key
 * @param skuId SKU identifier
 * @param warehouseId warehouse identifier
 * @param quantity reserved quantity
 * @param status reservation lifecycle status
 */
public record OrderReservationQueryResult(
        String reservationKey,
        Long skuId,
        Long warehouseId,
        long quantity,
        ReservationStatus status) {}
