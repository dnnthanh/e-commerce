package com.dnnthanh.marketplace.be.payment.api.domain.enumtype;

import com.dnnthanh.marketplace.be.platform.model.CodeEnum;

/** Normalized refund provider outcome persisted by the payment service. */
public enum RefundStatus implements CodeEnum {
    SUCCEEDED,
    FAILED,
    UNKNOWN
}
