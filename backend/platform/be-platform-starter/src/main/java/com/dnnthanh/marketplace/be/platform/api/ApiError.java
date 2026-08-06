package com.dnnthanh.marketplace.be.platform.api;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Stable API error contract.
 *
 * @param code machine-readable error code
 * @param message localized human-readable message
 * @param traceId distributed trace identifier
 * @param timestamp failure timestamp
 * @param path request path
 * @param fieldErrors field-level validation failures when applicable
 * @param details optional machine-readable error context
 */
public record ApiError(
        String code,
        String message,
        String traceId,
        LocalDateTime timestamp,
        String path,
        List<FieldError> fieldErrors,
        Map<String, Object> details) {

    /** Field-level validation failure. */
    public record FieldError(String field, String message) {}
}
