package com.dnnthanh.marketplace.be.payment.api.api.request;

import com.dnnthanh.marketplace.be.payment.api.domain.model.Payment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record InternalPaymentRequest(
        @NotBlank String paymentKey,
        @NotBlank String orderId,
        @NotBlank String userId,
        @NotNull Payment.Provider provider,
        @NotNull @Positive BigDecimal amount) {}
