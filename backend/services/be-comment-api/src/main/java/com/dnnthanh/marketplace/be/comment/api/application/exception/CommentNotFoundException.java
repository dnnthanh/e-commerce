package com.dnnthanh.marketplace.be.comment.api.application.exception;

import com.dnnthanh.marketplace.be.platform.exception.BusinessException;

/** Stable not-found error for comment threads. */
public class CommentNotFoundException extends BusinessException {
    public CommentNotFoundException() {
        super(CommentErrorCode.COMMENT_NOT_FOUND);
    }
}
