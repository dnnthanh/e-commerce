package com.dnnthanh.marketplace.be.order.api.api.response;

import com.dnnthanh.marketplace.be.order.api.domain.enumtype.CancellationReason;
import com.dnnthanh.marketplace.be.order.api.domain.enumtype.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Customer-visible parent order. */
public record OrderResponse(
        String orderNo,
        BigDecimal grossAmount,
        BigDecimal discountAmount,
        BigDecimal payableAmount,
        OrderStatus status,
        CancellationReason cancellationReason,
        List<SellerOrderResponse> sellerOrders,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {}
