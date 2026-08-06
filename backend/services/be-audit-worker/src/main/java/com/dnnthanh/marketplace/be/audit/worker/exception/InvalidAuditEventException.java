package com.dnnthanh.marketplace.be.audit.worker.exception;

/** Malformed audit event that is rejected rather than persisted with incomplete identity. */
public final class InvalidAuditEventException extends RuntimeException {
    public InvalidAuditEventException(String message) {
        super(message);
    }
}
