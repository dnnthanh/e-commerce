package com.dnnthanh.marketplace.be.payment.api.api.response;

import com.dnnthanh.marketplace.be.payment.api.domain.model.Payment;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(
        String paymentKey,
        String orderId,
        Payment.Provider provider,
        BigDecimal amount,
        Payment.Status status,
        String redirectUrl,
        LocalDateTime updatedAt) {}
