package com.dnnthanh.marketplace.be.inventory.api.application.service;

import com.dnnthanh.marketplace.be.inventory.api.application.port.out.InventoryOperationsPort;
import com.dnnthanh.marketplace.be.inventory.api.domain.exception.InvalidInventoryMutationException;
import com.dnnthanh.marketplace.be.platform.stereotype.UseCase;
import lombok.RequiredArgsConstructor;

/** Idempotent warehouse-to-warehouse transfer using deterministic lock ordering. */
@UseCase
@RequiredArgsConstructor
public class InventoryTransferService {
    private final InventoryOperationsPort operations;

    public TransferResult transfer(
            String transferKey,
            Long skuId,
            Long fromWarehouseId,
            Long toWarehouseId,
            long quantity) {
        if (transferKey == null || transferKey.isBlank()) {
            throw new InvalidInventoryMutationException("Transfer key is required");
        }
        if (fromWarehouseId.equals(toWarehouseId) || quantity <= 0) {
            throw new InvalidInventoryMutationException("Transfer warehouses/quantity are invalid");
        }
        boolean applied =
                operations.transfer(transferKey, skuId, fromWarehouseId, toWarehouseId, quantity);
        return new TransferResult(transferKey, applied);
    }

    public record TransferResult(String transferKey, boolean applied) {}
}
