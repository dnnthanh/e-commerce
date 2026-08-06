package com.dnnthanh.marketplace.be.payment.api.api.response;

import com.dnnthanh.marketplace.be.payment.api.domain.model.Payment;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentView(
        String paymentKey,
        String orderId,
        String userId,
        Payment.Provider provider,
        BigDecimal amount,
        Payment.Status status,
        LocalDateTime updatedAt) {}
