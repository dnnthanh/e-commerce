package com.dnnthanh.marketplace.be.payment.api.domain.enumtype;

import com.dnnthanh.marketplace.be.platform.model.CodeEnum;

/** Stable integration event names emitted by payment. */
public enum PaymentEventType implements CodeEnum {
    PAYMENT_PENDING,
    PAYMENT_SUCCEEDED,
    PAYMENT_FAILED,
    PAYMENT_UNKNOWN,
    PAYMENT_REFUNDED
}
