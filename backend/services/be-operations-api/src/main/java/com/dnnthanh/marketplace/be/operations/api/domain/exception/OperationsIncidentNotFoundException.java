package com.dnnthanh.marketplace.be.operations.api.domain.exception;

/** Requested open operational incident does not exist. */
public final class OperationsIncidentNotFoundException extends RuntimeException {
    public OperationsIncidentNotFoundException(Long id) {
        super("Open incident not found: " + id);
    }
}
