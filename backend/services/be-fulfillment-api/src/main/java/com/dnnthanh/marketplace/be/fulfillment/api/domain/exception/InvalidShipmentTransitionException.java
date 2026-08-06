package com.dnnthanh.marketplace.be.fulfillment.api.domain.exception;

import com.dnnthanh.marketplace.be.fulfillment.api.domain.enumtype.ShipmentStatus;

/** Raised when a shipment lifecycle transition violates fulfillment policy. */
public final class InvalidShipmentTransitionException extends RuntimeException {
    public InvalidShipmentTransitionException(ShipmentStatus current, ShipmentStatus next) {
        super("Invalid shipment transition: " + current + " -> " + next);
    }
}
