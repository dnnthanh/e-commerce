package com.dnnthanh.marketplace.be.checkout.api.api.request;

/** Checkout item supplied by an API caller. */
public record CheckoutItemRequest(Long sellerId, Long skuId, Long warehouseId, int quantity) {}
