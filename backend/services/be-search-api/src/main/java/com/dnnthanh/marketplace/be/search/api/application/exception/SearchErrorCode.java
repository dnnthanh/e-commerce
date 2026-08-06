package com.dnnthanh.marketplace.be.search.api.application.exception;

import com.dnnthanh.marketplace.be.platform.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SearchErrorCode implements ErrorCode {
    INVALID_SEARCH_CRITERIA("INVALID_SEARCH_CRITERIA", "error.validation", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String messageKey;
    private final HttpStatus httpStatus;
}
