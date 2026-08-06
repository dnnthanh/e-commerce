package com.dnnthanh.marketplace.be.inventory.api.api.response;

import com.dnnthanh.marketplace.be.inventory.api.domain.enumtype.ReservationStatus;

public record OrderReservationView(
        String reservationKey,
        Long skuId,
        Long warehouseId,
        long quantity,
        ReservationStatus status) {}
