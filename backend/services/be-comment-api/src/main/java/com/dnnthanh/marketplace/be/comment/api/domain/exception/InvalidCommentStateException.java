package com.dnnthanh.marketplace.be.comment.api.domain.exception;

/** Raised when a mutation is incompatible with the current comment lifecycle state. */
public class InvalidCommentStateException extends RuntimeException {
    public InvalidCommentStateException(String message) {
        super(message);
    }
}
