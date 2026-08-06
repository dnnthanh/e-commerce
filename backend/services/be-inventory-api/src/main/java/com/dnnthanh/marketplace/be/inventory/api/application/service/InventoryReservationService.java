package com.dnnthanh.marketplace.be.inventory.api.application.service;

import com.dnnthanh.marketplace.be.inventory.api.application.port.out.StockLedgerPort;
import com.dnnthanh.marketplace.be.inventory.api.domain.exception.InvalidInventoryMutationException;
import com.dnnthanh.marketplace.be.platform.stereotype.UseCase;
import lombok.RequiredArgsConstructor;

/** High-contention reservation commands delegated to an atomic persistence boundary. */
@UseCase
@RequiredArgsConstructor
public class InventoryReservationService {
    private final StockLedgerPort stockLedgerPort;

    public ReservationResult reserve(
            String requestKey, String sku, Long warehouseId, long quantity) {
        if (quantity <= 0)
            throw new InvalidInventoryMutationException("Reservation quantity must be positive");
        boolean newlyReserved =
                stockLedgerPort.reserveAtomically(requestKey, sku, warehouseId, quantity);
        return new ReservationResult(requestKey, newlyReserved);
    }

    public void confirm(String requestKey) {
        stockLedgerPort.confirmReservation(requestKey);
    }

    public void release(String requestKey, String reason) {
        stockLedgerPort.releaseReservation(requestKey, reason);
    }

    public record ReservationResult(String requestKey, boolean newlyReserved) {}
}
