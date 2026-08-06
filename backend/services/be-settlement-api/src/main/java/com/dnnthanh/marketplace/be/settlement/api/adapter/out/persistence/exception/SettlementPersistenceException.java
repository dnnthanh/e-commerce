package com.dnnthanh.marketplace.be.settlement.api.adapter.out.persistence.exception;

/** Named infrastructure failure while persisting settlement lifecycle/outbox state. */
public final class SettlementPersistenceException extends RuntimeException {
    public SettlementPersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
