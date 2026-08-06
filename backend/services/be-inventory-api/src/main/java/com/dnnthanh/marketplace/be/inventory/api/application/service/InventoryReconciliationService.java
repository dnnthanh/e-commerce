package com.dnnthanh.marketplace.be.inventory.api.application.service;

import com.dnnthanh.marketplace.be.inventory.api.application.port.out.InventoryOperationsPort;
import com.dnnthanh.marketplace.be.platform.stereotype.UseCase;
import java.util.List;
import lombok.RequiredArgsConstructor;

/** Detects drift between current balance, active reservations and immutable inventory ledger. */
@UseCase
@RequiredArgsConstructor
public class InventoryReconciliationService {
    private final InventoryOperationsPort operations;

    public List<InventoryOperationsPort.ReconciliationRow> reconcile(
            Long skuId, Long warehouseId, int requestedLimit) {
        return operations.reconcile(skuId, warehouseId, Math.max(1, Math.min(requestedLimit, 500)));
    }
}
