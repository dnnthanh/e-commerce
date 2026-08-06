package com.dnnthanh.marketplace.be.returns.api.application.exception;

import com.dnnthanh.marketplace.be.platform.exception.BusinessException;

/** Stable not-found response that does not disclose ownership information. */
public final class ReturnNotFoundException extends BusinessException {
    public ReturnNotFoundException() {
        super(ReturnErrorCode.RETURN_NOT_FOUND);
    }
}
