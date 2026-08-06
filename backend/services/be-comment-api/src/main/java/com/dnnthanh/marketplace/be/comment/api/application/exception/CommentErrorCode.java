package com.dnnthanh.marketplace.be.comment.api.application.exception;

import com.dnnthanh.marketplace.be.platform.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommentErrorCode implements ErrorCode {
    COMMENT_STATE_CONFLICT("COMMENT_STATE_CONFLICT", "error.conflict", HttpStatus.CONFLICT),
    COMMENT_FORBIDDEN("COMMENT_FORBIDDEN", "error.forbidden", HttpStatus.FORBIDDEN),
    COMMENT_NOT_FOUND("COMMENT_NOT_FOUND", "error.not-found", HttpStatus.NOT_FOUND),
    COMMENT_RATE_LIMITED(
            "COMMENT_RATE_LIMITED", "error.too-many-requests", HttpStatus.TOO_MANY_REQUESTS),
    INVALID_COMMENT("INVALID_COMMENT", "error.validation", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String messageKey;
    private final HttpStatus httpStatus;
}
