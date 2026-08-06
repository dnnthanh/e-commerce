package com.dnnthanh.marketplace.be.order.api.application.exception;

import com.dnnthanh.marketplace.be.platform.exception.BusinessException;

/** Stable bad-request error for invalid order input snapshots. */
public class InvalidOrderCommandException extends BusinessException {
    public InvalidOrderCommandException() {
        super(OrderErrorCode.INVALID_ORDER);
    }
}
