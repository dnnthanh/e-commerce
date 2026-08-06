package com.dnnthanh.marketplace.be.fulfillment.api.domain.exception;

/** Shipment was changed after it was read and must be reloaded before retry. */
public final class ShipmentConcurrentUpdateException extends RuntimeException {
    public ShipmentConcurrentUpdateException(String shipmentNo) {
        super("Shipment changed concurrently: " + shipmentNo);
    }
}
