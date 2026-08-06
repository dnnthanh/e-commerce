package com.dnnthanh.marketplace.be.comment.api.application.exception;

import com.dnnthanh.marketplace.be.platform.exception.BusinessException;

/** Stable conflict error for invalid comment lifecycle transitions. */
public class CommentConflictException extends BusinessException {
    public CommentConflictException() {
        super(CommentErrorCode.COMMENT_STATE_CONFLICT);
    }
}
