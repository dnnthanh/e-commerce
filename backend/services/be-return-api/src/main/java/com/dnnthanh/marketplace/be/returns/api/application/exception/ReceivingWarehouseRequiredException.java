package com.dnnthanh.marketplace.be.returns.api.application.exception;

import com.dnnthanh.marketplace.be.platform.exception.BusinessException;

/** Receiving warehouse is mandatory before returned inventory can be reconciled. */
public final class ReceivingWarehouseRequiredException extends BusinessException {
    public ReceivingWarehouseRequiredException() {
        super(ReturnErrorCode.WAREHOUSE_REQUIRED);
    }
}
