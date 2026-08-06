package com.dnnthanh.marketplace.be.checkout.api.domain.enumtype;

import com.dnnthanh.marketplace.be.platform.model.CodeEnum;

/** Durable checkout process-manager state. */
public enum CheckoutStep implements CodeEnum {
    CREATED,
    QUOTED,
    PROMOTION_RESERVED,
    INVENTORY_RESERVED,
    PAYMENT_PENDING,
    ORDER_CREATED,
    COMPLETED,
    COMPENSATING,
    FAILED
}
