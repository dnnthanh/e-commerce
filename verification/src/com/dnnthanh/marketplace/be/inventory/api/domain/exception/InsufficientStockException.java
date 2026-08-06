package com.dnnthanh.marketplace.be.inventory.api.domain.exception;

/** Dependency-free smoke-test stand-in; production hierarchy is verified separately. */
public final class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String sku, Long warehouseId, long requested, long available) {
        super("Insufficient stock");
    }

    public InsufficientStockException(String sku, long requested, long available) {
        this(sku, null, requested, available);
    }
}
