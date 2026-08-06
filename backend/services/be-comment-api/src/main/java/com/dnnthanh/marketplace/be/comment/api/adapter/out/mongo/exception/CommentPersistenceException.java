package com.dnnthanh.marketplace.be.comment.api.adapter.out.mongo.exception;

/** Infrastructure error raised when a comment outbox payload cannot be serialized. */
public class CommentPersistenceException extends RuntimeException {
    public CommentPersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
