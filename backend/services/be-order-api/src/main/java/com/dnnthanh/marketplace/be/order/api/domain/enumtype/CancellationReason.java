package com.dnnthanh.marketplace.be.order.api.domain.enumtype;

import com.dnnthanh.marketplace.be.platform.model.CodeEnum;

/** Closed set of reasons that may cancel an order. */
public enum CancellationReason implements CodeEnum {
    CUSTOMER_REQUEST,
    PAYMENT_TIMEOUT,
    SELLER_REJECTED,
    FRAUD_REJECTED
}
