package com.dnnthanh.marketplace.be.order.api.domain.enumtype;

import com.dnnthanh.marketplace.be.platform.model.CodeEnum;

/** Seller child-order lifecycle. */
public enum SellerOrderStatus implements CodeEnum {
    CREATED,
    PAID,
    FULFILLING,
    COMPLETED,
    CANCELLED,
    EXPIRED
}
