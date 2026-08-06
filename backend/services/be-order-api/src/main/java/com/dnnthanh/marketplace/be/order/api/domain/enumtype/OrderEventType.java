package com.dnnthanh.marketplace.be.order.api.domain.enumtype;

import com.dnnthanh.marketplace.be.platform.model.CodeEnum;

/** Durable events emitted by the Order bounded context. */
public enum OrderEventType implements CodeEnum {
    ORDER_CREATED,
    ORDER_PAYMENT_PENDING,
    ORDER_PAID,
    ORDER_FULFILLING,
    ORDER_COMPLETED,
    ORDER_CANCELLED,
    ORDER_EXPIRED,
    SELLER_ORDER_CANCELLED,
    ORDER_MANUAL_OVERRIDE
}
