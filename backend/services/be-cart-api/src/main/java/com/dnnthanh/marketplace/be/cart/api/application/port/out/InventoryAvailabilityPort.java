package com.dnnthanh.marketplace.be.cart.api.application.port.out;

/** Authoritative Inventory availability required before Checkout. */
public interface InventoryAvailabilityPort {
    InventorySnapshot getBySku(String sku);

    record InventorySnapshot(String sku, long available) {}
}
