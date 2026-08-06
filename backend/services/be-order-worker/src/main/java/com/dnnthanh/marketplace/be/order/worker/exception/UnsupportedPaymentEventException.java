package com.dnnthanh.marketplace.be.order.worker.exception;

/** Payment event cannot be mapped to an Order transition. */
public final class UnsupportedPaymentEventException extends RuntimeException {
    public UnsupportedPaymentEventException(String eventType) {
        super("Unsupported payment event " + eventType);
    }
}
