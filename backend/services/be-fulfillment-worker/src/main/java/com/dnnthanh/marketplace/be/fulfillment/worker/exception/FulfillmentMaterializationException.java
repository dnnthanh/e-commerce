package com.dnnthanh.marketplace.be.fulfillment.worker.exception;

/** Order/reservation data is insufficient to build a fulfillment shipment. */
public final class FulfillmentMaterializationException extends RuntimeException {
    public FulfillmentMaterializationException(String message) {
        super(message);
    }
}
