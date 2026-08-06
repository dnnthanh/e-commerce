package com.dnnthanh.marketplace.be.checkout.api.application.model;

import com.dnnthanh.marketplace.be.checkout.api.domain.enumtype.CheckoutPaymentStatus;
import com.dnnthanh.marketplace.be.checkout.api.domain.model.CheckoutSaga;

public record CheckoutResult(
        String checkoutKey,
        CheckoutSaga.State status,
        String orderNo,
        CheckoutPaymentStatus paymentStatus,
        String redirectUrl) {}
