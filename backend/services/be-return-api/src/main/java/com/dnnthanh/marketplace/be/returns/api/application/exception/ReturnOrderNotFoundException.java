package com.dnnthanh.marketplace.be.returns.api.application.exception;

import com.dnnthanh.marketplace.be.platform.exception.BusinessException;

/** Order is absent or is not owned by the requesting customer. */
public final class ReturnOrderNotFoundException extends BusinessException {
    public ReturnOrderNotFoundException() {
        super(ReturnErrorCode.ORDER_NOT_FOUND);
    }
}
