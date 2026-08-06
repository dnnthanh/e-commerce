package com.dnnthanh.marketplace.be.platform.exception;

import java.io.Serial;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import lombok.Getter;

/**
 * Base application exception carrying a typed error definition and optional localization context.
 */
@Getter
public class BusinessException extends RuntimeException {

    @Serial private static final long serialVersionUID = 1L;

    private final transient ErrorCode errorCode;
    private final transient Object[] messageArguments;
    private final transient Map<String, Object> details;

    public BusinessException(ErrorCode errorCode) {
        this(errorCode, null, Map.of());
    }

    public BusinessException(ErrorCode errorCode, Object... messageArguments) {
        this(errorCode, errorCode.getCode(), messageArguments, Map.of(), null);
    }

    /**
     * Keeps diagnostic detail for logs while the HTTP message remains localized from the error
     * code.
     */
    public BusinessException(ErrorCode errorCode, String detailMessage) {
        this(errorCode, detailMessage, null, Map.of(), null);
    }

    public BusinessException(
            ErrorCode errorCode, Object[] messageArguments, Map<String, Object> details) {
        this(errorCode, errorCode.getCode(), messageArguments, details, null);
    }

    public BusinessException(
            ErrorCode errorCode,
            String detailMessage,
            Object[] messageArguments,
            Map<String, Object> details,
            Throwable cause) {
        super(detailMessage, cause);
        Objects.requireNonNull(errorCode, "errorCode");
        this.errorCode = errorCode;
        this.messageArguments =
                messageArguments == null
                        ? new Object[0]
                        : Arrays.copyOf(messageArguments, messageArguments.length);
        this.details = details == null ? Map.of() : Map.copyOf(details);
    }
}
