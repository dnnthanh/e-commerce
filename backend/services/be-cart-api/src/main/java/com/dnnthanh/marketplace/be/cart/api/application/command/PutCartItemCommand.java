package com.dnnthanh.marketplace.be.cart.api.application.command;

import java.math.BigDecimal;

/** Application command for a versioned cart item mutation. */
public record PutCartItemCommand(
        Long sellerId,
        Long skuId,
        int quantity,
        BigDecimal priceSnapshot,
        boolean selected,
        long expectedVersion) {}
