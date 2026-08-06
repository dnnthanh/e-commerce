package com.dnnthanh.marketplace.be.catalog.api.application.dto;

/** Authoritative Catalog facts required to validate a Cart/Checkout line. */
public record SkuCheckoutSnapshot(
        Long skuId, Long productId, Long sellerId, boolean active, int purchaseLimit) {}
