package com.dnnthanh.marketplace.be.settlement.worker.exception;

/** Fulfillment data cannot be materialized into seller settlement ledger entries. */
public final class SettlementMaterializationException extends RuntimeException {
    public SettlementMaterializationException(String message) {
        super(message);
    }
}
