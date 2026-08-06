package com.dnnthanh.marketplace.be.review.api.domain.exception;

/** Caller is not allowed to mutate this review. */
public final class ReviewPermissionException extends RuntimeException {
    public ReviewPermissionException(String message) {
        super(message);
    }
}
