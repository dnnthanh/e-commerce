package com.dnnthanh.marketplace.be.order.api.api.response;

import com.dnnthanh.marketplace.be.order.api.domain.enumtype.SellerOrderStatus;
import java.math.BigDecimal;
import java.util.List;

/** Trusted downstream snapshot of a seller child order. */
public record InternalSellerOrderResponse(
        Long sellerId,
        String sellerOrderNo,
        BigDecimal grossAmount,
        BigDecimal discountAmount,
        BigDecimal payableAmount,
        SellerOrderStatus status,
        List<OrderLineResponse> lines) {}
