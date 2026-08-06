package com.dnnthanh.marketplace.be.comment.api.domain.exception;

/** Raised when a user attempts an owner-only comment mutation. */
public class CommentPermissionException extends RuntimeException {
    public CommentPermissionException() {
        super("Comment mutation is not allowed for this user");
    }
}
