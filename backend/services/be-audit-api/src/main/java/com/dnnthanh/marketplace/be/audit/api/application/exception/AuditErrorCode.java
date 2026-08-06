package com.dnnthanh.marketplace.be.audit.api.application.exception;

import com.dnnthanh.marketplace.be.platform.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuditErrorCode implements ErrorCode {
    INVALID_AUDIT_SEARCH_CRITERIA(
            "INVALID_AUDIT_SEARCH_CRITERIA", "error.validation", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String messageKey;
    private final HttpStatus httpStatus;
}
