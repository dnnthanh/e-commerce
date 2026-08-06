package com.dnnthanh.marketplace.be.payment.api.domain.exception;

/** No provider adapter is registered for the requested provider. */
public final class UnsupportedPaymentProviderException extends RuntimeException {
    public UnsupportedPaymentProviderException(String message) {
        super(message);
    }
}
