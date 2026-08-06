package com.dnnthanh.marketplace.be.review.api.adapter.out.persistence.exception;

/** Infrastructure failure while persisting a review or its transactional outbox event. */
public class ReviewPersistenceException extends RuntimeException {
    public ReviewPersistenceException(String message) {
        super(message);
    }

    public ReviewPersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
