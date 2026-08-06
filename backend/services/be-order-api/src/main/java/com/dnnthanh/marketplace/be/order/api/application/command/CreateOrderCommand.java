package com.dnnthanh.marketplace.be.order.api.application.command;

import java.math.BigDecimal;
import java.util.List;

/** Trusted immutable checkout snapshot used to create an Order aggregate idempotently. */
public record CreateOrderCommand(
        String checkoutKey,
        String userId,
        List<Line> lines,
        BigDecimal grossAmount,
        BigDecimal discountAmount) {

    /** One repriced checkout line. */
    public record Line(Long sellerId, Long skuId, int quantity, BigDecimal unitPrice) {}
}
