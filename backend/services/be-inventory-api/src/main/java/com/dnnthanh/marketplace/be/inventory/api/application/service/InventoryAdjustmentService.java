package com.dnnthanh.marketplace.be.inventory.api.application.service;

import com.dnnthanh.marketplace.be.inventory.api.application.port.out.InventoryOperationsPort;
import com.dnnthanh.marketplace.be.inventory.api.domain.enumtype.InventoryAdjustmentReason;
import com.dnnthanh.marketplace.be.inventory.api.domain.exception.InvalidInventoryMutationException;
import com.dnnthanh.marketplace.be.platform.stereotype.UseCase;
import lombok.RequiredArgsConstructor;

/** Idempotent physical stock adjustment with explicit audit reason. */
@UseCase
@RequiredArgsConstructor
public class InventoryAdjustmentService {
    private final InventoryOperationsPort operations;

    public AdjustmentResult adjust(
            String referenceKey,
            Long skuId,
            Long warehouseId,
            long delta,
            InventoryAdjustmentReason reason) {
        if (referenceKey == null || referenceKey.isBlank()) {
            throw new InvalidInventoryMutationException("Adjustment reference key is required");
        }
        if (delta == 0) {
            throw new InvalidInventoryMutationException("Adjustment delta cannot be zero");
        }
        boolean applied = operations.adjust(referenceKey, skuId, warehouseId, delta, reason.name());
        return new AdjustmentResult(referenceKey, applied);
    }

    public record AdjustmentResult(String referenceKey, boolean applied) {}
}
