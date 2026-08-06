package com.dnnthanh.marketplace.be.inventory.api.application.port.out;

import java.util.List;

/** Atomic operational inventory mutations and reconciliation boundary. */
public interface InventoryOperationsPort {
    boolean adjust(String referenceKey, Long skuId, Long warehouseId, long delta, String reason);

    boolean transfer(
            String transferKey,
            Long skuId,
            Long fromWarehouseId,
            Long toWarehouseId,
            long quantity);

    List<ReconciliationRow> reconcile(Long skuId, Long warehouseId, int limit);

    record ReconciliationRow(
            Long skuId,
            Long warehouseId,
            long onHand,
            long storedReserved,
            long activeReservationQuantity,
            long reservedDrift,
            long ledgerNetDelta) {}
}
