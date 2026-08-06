package com.dnnthanh.marketplace.be.order.api.application.exception;

import com.dnnthanh.marketplace.be.platform.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum OrderErrorCode implements ErrorCode {
    INVALID_ORDER("INVALID_ORDER", "error.validation", HttpStatus.BAD_REQUEST),
    ORDER_NOT_FOUND("ORDER_NOT_FOUND", "error.not-found", HttpStatus.NOT_FOUND),
    ORDER_STATE_CONFLICT("ORDER_STATE_CONFLICT", "error.conflict", HttpStatus.CONFLICT);

    private final String code;
    private final String messageKey;
    private final HttpStatus httpStatus;
}
