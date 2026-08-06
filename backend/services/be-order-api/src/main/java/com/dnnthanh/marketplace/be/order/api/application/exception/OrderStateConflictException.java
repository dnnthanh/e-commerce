package com.dnnthanh.marketplace.be.order.api.application.exception;

import com.dnnthanh.marketplace.be.platform.exception.BusinessException;

/** Stable conflict error for invalid order lifecycle transitions. */
public class OrderStateConflictException extends BusinessException {
    public OrderStateConflictException() {
        super(OrderErrorCode.ORDER_STATE_CONFLICT);
    }
}
