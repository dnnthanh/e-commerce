package com.dnnthanh.marketplace.be.payment.api.application.command;

import com.dnnthanh.marketplace.be.payment.api.domain.model.Payment;
import java.math.BigDecimal;

public record CreatePaymentCommand(
        String paymentKey, String orderId, Payment.Provider provider, BigDecimal amount) {}
