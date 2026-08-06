package com.dnnthanh.marketplace.be.checkout.api.application.model;

/** Checkout item passed through the application layer. */
public record CheckoutItemCommand(Long sellerId, Long skuId, Long warehouseId, int quantity) {}
