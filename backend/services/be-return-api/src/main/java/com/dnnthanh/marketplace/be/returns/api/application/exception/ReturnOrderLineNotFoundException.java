package com.dnnthanh.marketplace.be.returns.api.application.exception;

import com.dnnthanh.marketplace.be.platform.exception.BusinessException;

/** Selected order line does not exist in the immutable customer order snapshot. */
public final class ReturnOrderLineNotFoundException extends BusinessException {
    public ReturnOrderLineNotFoundException() {
        super(ReturnErrorCode.ORDER_LINE_NOT_FOUND);
    }
}
