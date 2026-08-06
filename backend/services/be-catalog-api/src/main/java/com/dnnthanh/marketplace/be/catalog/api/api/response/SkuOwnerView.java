package com.dnnthanh.marketplace.be.catalog.api.api.response;

public record SkuOwnerView(
        Long skuId, Long productId, Long sellerId, boolean active, int purchaseLimit) {}
