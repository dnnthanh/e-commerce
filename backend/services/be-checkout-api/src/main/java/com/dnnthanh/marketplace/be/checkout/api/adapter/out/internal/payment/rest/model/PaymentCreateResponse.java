package com.dnnthanh.marketplace.be.checkout.api.adapter.out.internal.payment.rest.model;

import com.dnnthanh.marketplace.be.checkout.api.domain.enumtype.CheckoutPaymentStatus;

public record PaymentCreateResponse(CheckoutPaymentStatus status, String redirectUrl) {}
