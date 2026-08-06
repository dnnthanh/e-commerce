package com.dnnthanh.marketplace.be.returns.api.application.exception;

import com.dnnthanh.marketplace.be.platform.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ReturnErrorCode implements ErrorCode {
    WAREHOUSE_REQUIRED("WAREHOUSE_REQUIRED", "error.validation", HttpStatus.BAD_REQUEST),
    RETURN_NOT_FOUND("RETURN_NOT_FOUND", "error.not-found", HttpStatus.NOT_FOUND),
    ORDER_NOT_FOUND("ORDER_NOT_FOUND", "error.not-found", HttpStatus.NOT_FOUND),
    ORDER_LINE_NOT_FOUND("ORDER_LINE_NOT_FOUND", "error.not-found", HttpStatus.NOT_FOUND);

    private final String code;
    private final String messageKey;
    private final HttpStatus httpStatus;
}
