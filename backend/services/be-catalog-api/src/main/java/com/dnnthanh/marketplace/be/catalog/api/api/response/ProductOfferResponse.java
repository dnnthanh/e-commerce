package com.dnnthanh.marketplace.be.catalog.api.api.response;

/** Public customer-safe representation of one sellable SKU/offer. */
public record ProductOfferResponse(
        Long skuId,
        Long productId,
        Long sellerId,
        String productName,
        String sellerSku,
        String variantName,
        int purchaseLimit) {}
