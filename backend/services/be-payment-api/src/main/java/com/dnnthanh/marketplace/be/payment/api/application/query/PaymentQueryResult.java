package com.dnnthanh.marketplace.be.payment.api.application.query;

import com.dnnthanh.marketplace.be.payment.api.domain.model.Payment;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Read-model row for payment operations/finance search. */
public record PaymentQueryResult(
        String paymentKey,
        String orderId,
        String userId,
        Payment.Provider provider,
        BigDecimal amount,
        Payment.Status status,
        LocalDateTime updatedAt) {}
