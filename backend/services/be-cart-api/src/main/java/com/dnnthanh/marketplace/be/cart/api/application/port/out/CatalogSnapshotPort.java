package com.dnnthanh.marketplace.be.cart.api.application.port.out;

/** Authoritative Catalog snapshot required before Checkout. */
public interface CatalogSnapshotPort {
    CatalogSnapshot getBySku(String sku);

    record CatalogSnapshot(String sku, Long sellerId, boolean active, int purchaseLimit) {}
}
