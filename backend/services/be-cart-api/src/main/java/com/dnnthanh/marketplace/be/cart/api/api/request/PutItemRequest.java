package com.dnnthanh.marketplace.be.cart.api.api.request;

import java.math.BigDecimal;

public record PutItemRequest(
        Long sellerId,
        Long skuId,
        int quantity,
        BigDecimal priceSnapshot,
        boolean selected,
        long expectedVersion) {}
