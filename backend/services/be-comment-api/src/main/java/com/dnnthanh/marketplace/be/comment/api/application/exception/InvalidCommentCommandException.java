package com.dnnthanh.marketplace.be.comment.api.application.exception;

import com.dnnthanh.marketplace.be.platform.exception.BusinessException;

/** Stable bad-request error for invalid comment input. */
public class InvalidCommentCommandException extends BusinessException {
    public InvalidCommentCommandException() {
        super(CommentErrorCode.INVALID_COMMENT);
    }
}
