package com.dnnthanh.marketplace.be.checkout.api.domain.enumtype;

import com.dnnthanh.marketplace.be.platform.model.CodeEnum;

/** Normalized payment state understood by the checkout process manager. */
public enum CheckoutPaymentStatus implements CodeEnum {
    PAID,
    PENDING,
    UNKNOWN,
    FAILED
}
