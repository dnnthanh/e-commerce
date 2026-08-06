package com.dnnthanh.marketplace.be.checkout.api.api.response;

import com.dnnthanh.marketplace.be.checkout.api.domain.enumtype.CheckoutPaymentStatus;
import com.dnnthanh.marketplace.be.checkout.api.domain.model.CheckoutSaga;

public record CheckoutResponse(
        String checkoutKey,
        CheckoutSaga.State status,
        String orderNo,
        CheckoutPaymentStatus paymentStatus,
        String redirectUrl) {}
