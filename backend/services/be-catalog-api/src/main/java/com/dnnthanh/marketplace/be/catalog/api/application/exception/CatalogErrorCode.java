package com.dnnthanh.marketplace.be.catalog.api.application.exception;

import com.dnnthanh.marketplace.be.platform.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CatalogErrorCode implements ErrorCode {
    PRODUCT_NOT_FOUND("PRODUCT_NOT_FOUND", "error.not-found", HttpStatus.NOT_FOUND),
    INVALID_PRODUCT("INVALID_PRODUCT", "error.validation", HttpStatus.BAD_REQUEST),
    PRODUCT_STATE_CONFLICT("PRODUCT_STATE_CONFLICT", "error.conflict", HttpStatus.CONFLICT);

    private final String code;
    private final String messageKey;
    private final HttpStatus httpStatus;
}
