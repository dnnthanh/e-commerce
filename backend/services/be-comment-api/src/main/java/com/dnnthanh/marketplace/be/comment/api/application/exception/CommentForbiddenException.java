package com.dnnthanh.marketplace.be.comment.api.application.exception;

import com.dnnthanh.marketplace.be.platform.exception.BusinessException;

/** Stable forbidden error for owner-only comment mutations. */
public class CommentForbiddenException extends BusinessException {
    public CommentForbiddenException() {
        super(CommentErrorCode.COMMENT_FORBIDDEN);
    }
}
