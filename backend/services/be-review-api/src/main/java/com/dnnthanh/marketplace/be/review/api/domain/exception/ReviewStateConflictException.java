package com.dnnthanh.marketplace.be.review.api.domain.exception;

/** Review action conflicts with edit-window or moderation lifecycle. */
public final class ReviewStateConflictException extends RuntimeException {
    public ReviewStateConflictException(String message) {
        super(message);
    }
}
