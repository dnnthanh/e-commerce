package com.dnnthanh.marketplace.be.payment.api.api.request;

import com.dnnthanh.marketplace.be.payment.api.domain.model.Payment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record CreatePaymentRequest(
        @NotBlank String paymentKey,
        @NotBlank String orderId,
        @NotNull Payment.Provider provider,
        @NotNull @Positive BigDecimal amount) {}
