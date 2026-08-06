package com.dnnthanh.marketplace.be.order.api.api.response;

import java.math.BigDecimal;

/** Immutable customer/internal order-line snapshot. */
public record OrderLineResponse(
        Long orderLineId,
        Long sellerId,
        Long skuId,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal allocatedDiscount,
        BigDecimal netAmount) {}
