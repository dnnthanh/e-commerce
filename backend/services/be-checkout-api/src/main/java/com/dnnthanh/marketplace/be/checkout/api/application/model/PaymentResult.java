package com.dnnthanh.marketplace.be.checkout.api.application.model;

import com.dnnthanh.marketplace.be.checkout.api.domain.enumtype.CheckoutPaymentStatus;

public record PaymentResult(CheckoutPaymentStatus status, String redirectUrl) {}
