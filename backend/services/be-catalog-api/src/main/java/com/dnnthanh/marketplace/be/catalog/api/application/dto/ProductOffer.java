package com.dnnthanh.marketplace.be.catalog.api.application.dto;

/** Customer-safe Catalog projection for one sellable product SKU. */
public record ProductOffer(
        Long skuId,
        Long productId,
        Long sellerId,
        String productName,
        String sellerSku,
        String variantName,
        int purchaseLimit) {}
