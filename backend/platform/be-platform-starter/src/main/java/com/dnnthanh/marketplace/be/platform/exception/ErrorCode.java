package com.dnnthanh.marketplace.be.platform.exception;

import com.dnnthanh.marketplace.be.platform.i18n.MessageResolvable;
import com.dnnthanh.marketplace.be.platform.model.CodeEnum;
import org.springframework.http.HttpStatus;

/** Typed API error definition combining stable code, i18n key and HTTP semantics. */
public interface ErrorCode extends CodeEnum, MessageResolvable {

    @Override
    default String getDefaultMessage() {
        return getCode();
    }

    /** HTTP status emitted when the error crosses an HTTP boundary. */
    HttpStatus getHttpStatus();
}
