package com.dnnthanh.marketplace.be.checkout.api.adapter.out.internal.payment.rest.model;

import com.dnnthanh.marketplace.be.checkout.api.application.model.PaymentProvider;
import java.math.BigDecimal;

public record PaymentCreateRequest(
        String paymentKey,
        String orderId,
        String userId,
        PaymentProvider provider,
        BigDecimal amount) {}
