package com.dnnthanh.marketplace.be.promotion.api.adapter.out.persistence.exception;

/** Infrastructure failure while translating or persisting promotion configuration. */
public final class PromotionPersistenceException extends RuntimeException {
    public PromotionPersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
