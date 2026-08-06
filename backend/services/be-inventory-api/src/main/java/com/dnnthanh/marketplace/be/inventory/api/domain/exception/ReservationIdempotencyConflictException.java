package com.dnnthanh.marketplace.be.inventory.api.domain.exception;

/** Same reservation key was reused with different business input. */
public final class ReservationIdempotencyConflictException extends RuntimeException {
    public ReservationIdempotencyConflictException(String key) {
        super("Reservation idempotency conflict: " + key);
    }
}
