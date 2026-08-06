package com.dnnthanh.marketplace.be.inventory.api.api.response;

import com.dnnthanh.marketplace.be.inventory.api.domain.enumtype.ReservationStatus;
import java.time.LocalDateTime;

public record ReservationResponse(
        String reservationKey,
        Long skuId,
        Long warehouseId,
        long quantity,
        ReservationStatus status,
        LocalDateTime expiresAt) {}
