package com.dnnthanh.marketplace.be.order.api.api.response;

import com.dnnthanh.marketplace.be.order.api.domain.enumtype.CancellationReason;
import com.dnnthanh.marketplace.be.order.api.domain.enumtype.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Complete order snapshot for trusted bounded contexts. */
public record InternalOrderResponse(
        String orderNo,
        String checkoutKey,
        String userId,
        BigDecimal grossAmount,
        BigDecimal discountAmount,
        BigDecimal payableAmount,
        OrderStatus status,
        CancellationReason cancellationReason,
        List<InternalSellerOrderResponse> sellerOrders,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        long version) {}
