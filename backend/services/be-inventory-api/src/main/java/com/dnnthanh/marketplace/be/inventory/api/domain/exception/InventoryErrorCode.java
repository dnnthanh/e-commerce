package com.dnnthanh.marketplace.be.inventory.api.domain.exception;

import com.dnnthanh.marketplace.be.platform.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum InventoryErrorCode implements ErrorCode {
    INSUFFICIENT_STOCK("INSUFFICIENT_STOCK", "error.inventory.insufficient", HttpStatus.CONFLICT);

    private final String code;
    private final String messageKey;
    private final HttpStatus httpStatus;
}
