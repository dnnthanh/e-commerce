package com.dnnthanh.marketplace.be.order.api.api.response;

import com.dnnthanh.marketplace.be.order.api.domain.enumtype.SellerOrderStatus;
import java.math.BigDecimal;

/** Customer-visible seller child-order summary. */
public record SellerOrderResponse(
        Long sellerId, String sellerOrderNo, BigDecimal payableAmount, SellerOrderStatus status) {}
