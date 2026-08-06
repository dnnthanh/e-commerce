package com.dnnthanh.marketplace.be.comment.api.domain.exception;

/** Raised when comment content or identifiers violate domain invariants. */
public class InvalidCommentException extends RuntimeException {
    public InvalidCommentException(String message) {
        super(message);
    }
}
