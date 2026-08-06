package com.dnnthanh.marketplace.be.payment.api.api.request;

import java.math.BigDecimal;

public record InternalRefundRequest(
        String refundKey, String orderId, String returnKey, BigDecimal amount) {}
