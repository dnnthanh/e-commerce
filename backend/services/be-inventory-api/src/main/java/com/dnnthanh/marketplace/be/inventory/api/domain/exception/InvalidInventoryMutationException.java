package com.dnnthanh.marketplace.be.inventory.api.domain.exception;

/** Inventory command would violate balance or quantity invariants. */
public final class InvalidInventoryMutationException extends RuntimeException {
    public InvalidInventoryMutationException(String message) {
        super(message);
    }
}
