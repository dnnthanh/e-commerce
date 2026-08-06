package com.dnnthanh.marketplace.be.order.api.domain.enumtype;

import com.dnnthanh.marketplace.be.platform.model.CodeEnum;

/** Parent marketplace-order lifecycle. */
public enum OrderStatus implements CodeEnum {
    CREATED,
    PAYMENT_PENDING,
    PAID,
    FULFILLING,
    COMPLETED,
    CANCELLED,
    EXPIRED
}
