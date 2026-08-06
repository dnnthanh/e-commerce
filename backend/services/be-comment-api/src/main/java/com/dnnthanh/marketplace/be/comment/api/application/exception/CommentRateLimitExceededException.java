package com.dnnthanh.marketplace.be.comment.api.application.exception;

import com.dnnthanh.marketplace.be.platform.exception.BusinessException;

/** Stable API error for comment anti-spam throttling. */
public class CommentRateLimitExceededException extends BusinessException {
    public CommentRateLimitExceededException() {
        super(CommentErrorCode.COMMENT_RATE_LIMITED);
    }
}
