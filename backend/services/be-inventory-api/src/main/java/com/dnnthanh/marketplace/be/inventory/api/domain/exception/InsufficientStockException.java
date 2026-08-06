package com.dnnthanh.marketplace.be.inventory.api.domain.exception;

import com.dnnthanh.marketplace.be.platform.exception.BusinessException;

/** Atomic reservation cannot be satisfied from current available stock. */
public final class InsufficientStockException extends BusinessException {

    public InsufficientStockException(
            String sku, Long warehouseId, long requested, long available) {
        super(
                InventoryErrorCode.INSUFFICIENT_STOCK,
                "Insufficient stock for sku=%s, warehouse=%s: requested=%d, available=%d"
                        .formatted(sku, warehouseId, requested, available));
    }

    /** Backward-compatible constructor used by the contention-focused StockLedger model. */
    public InsufficientStockException(String sku, long requested, long available) {
        this(sku, null, requested, available);
    }
}
