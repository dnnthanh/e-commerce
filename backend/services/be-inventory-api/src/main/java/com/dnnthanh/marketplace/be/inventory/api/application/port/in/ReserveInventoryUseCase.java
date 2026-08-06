package com.dnnthanh.marketplace.be.inventory.api.application.port.in;

import com.dnnthanh.marketplace.be.inventory.api.domain.model.Reservation;

public interface ReserveInventoryUseCase {
    Reservation reserve(
            String reservationKey, Long skuId, Long warehouseId, long quantity, int ttlMinutes);
}
