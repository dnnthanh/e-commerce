package com.dnnthanh.marketplace.be.order.api.application.exception;

import com.dnnthanh.marketplace.be.platform.exception.BusinessException;

/** Stable application error used both for absent and non-owned orders to avoid existence leaks. */
public class OrderNotFoundException extends BusinessException {
    public OrderNotFoundException() {
        super(OrderErrorCode.ORDER_NOT_FOUND);
    }
}
