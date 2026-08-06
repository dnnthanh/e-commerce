package com.dnnthanh.marketplace.be.settlement.api.domain.exception;

/** Settlement command conflicts with the current period state. */
public final class SettlementStateConflictException extends RuntimeException {
    public SettlementStateConflictException(String message) {
        super(message);
    }
}
