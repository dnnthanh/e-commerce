package com.dnnthanh.marketplace.be.fulfillment.api.domain.exception;

/** Shipment definition violates line/quantity requirements. */
public final class InvalidShipmentException extends RuntimeException {
    public InvalidShipmentException(String message) {
        super(message);
    }
}
