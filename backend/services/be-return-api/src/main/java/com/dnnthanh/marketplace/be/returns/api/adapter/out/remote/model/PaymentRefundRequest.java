package com.dnnthanh.marketplace.be.returns.api.adapter.out.remote.model;

import java.math.BigDecimal;

public record PaymentRefundRequest(
        String refundKey, String orderId, String returnKey, BigDecimal amount) {}
