package com.dnnthanh.marketplace.be.payment.api.application.command;

import com.dnnthanh.marketplace.be.payment.api.domain.model.Payment;
import java.math.BigDecimal;

public record InternalCreatePaymentCommand(
        String paymentKey,
        String orderId,
        String userId,
        Payment.Provider provider,
        BigDecimal amount) {}
