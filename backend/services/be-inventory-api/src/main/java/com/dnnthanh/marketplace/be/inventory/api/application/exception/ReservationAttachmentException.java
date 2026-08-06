package com.dnnthanh.marketplace.be.inventory.api.application.exception;

/** Reservation cannot be attached to the requested final order. */
public final class ReservationAttachmentException extends RuntimeException {

    public ReservationAttachmentException(String reservationKey, String orderId) {
        super("Reservation " + reservationKey + " cannot be attached to order " + orderId);
    }
}
