package com.dnnthanh.marketplace.be.payment.api.domain.exception;

/** Refund amount exceeds or otherwise violates captured-payment constraints. */
public final class InvalidRefundAmountException extends RuntimeException {
    public InvalidRefundAmountException(String message) {
        super(message);
    }
}
