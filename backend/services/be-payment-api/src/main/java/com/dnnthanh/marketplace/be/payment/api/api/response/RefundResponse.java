package com.dnnthanh.marketplace.be.payment.api.api.response;

import com.dnnthanh.marketplace.be.payment.api.domain.enumtype.RefundStatus;
import com.dnnthanh.marketplace.be.payment.api.domain.model.Payment;
import java.math.BigDecimal;

public record RefundResponse(
        String refundKey, RefundStatus status, BigDecimal amount, Payment.Status paymentStatus) {}
