package com.dnnthanh.marketplace.be.review.api.domain.exception;

/** Review content/rating violates review invariants. */
public final class InvalidReviewException extends RuntimeException {
    public InvalidReviewException(String message) {
        super(message);
    }
}
