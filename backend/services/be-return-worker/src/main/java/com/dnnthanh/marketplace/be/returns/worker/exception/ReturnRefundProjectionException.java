package com.dnnthanh.marketplace.be.returns.worker.exception;

/** Refund completion cannot be projected into the return/inventory workflow. */
public final class ReturnRefundProjectionException extends RuntimeException {
    public ReturnRefundProjectionException(String message) {
        super(message);
    }
}
