package com.dnnthanh.marketplace.be.platform.web.error;

import com.dnnthanh.marketplace.be.platform.api.ApiError;
import com.dnnthanh.marketplace.be.platform.exception.ErrorCode;
import com.dnnthanh.marketplace.be.platform.i18n.MessageResolver;
import com.dnnthanh.marketplace.be.platform.trace.TraceContextAccessor;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Builds localized API errors consistently for MVC and Spring Security boundaries. */
@Component
@RequiredArgsConstructor
public class ApiErrorFactory {

    private final MessageResolver messageResolver;
    private final TraceContextAccessor traceContext;

    public ApiError create(ErrorCode errorCode, HttpServletRequest request) {
        return create(errorCode, request, request.getLocale(), null, null, null);
    }

    public ApiError create(ErrorCode errorCode, HttpServletRequest request, Locale locale) {
        return create(errorCode, request, locale, null, null, null);
    }

    public ApiError create(
            ErrorCode errorCode,
            HttpServletRequest request,
            Locale locale,
            Object[] messageArguments,
            List<ApiError.FieldError> fieldErrors,
            Map<String, Object> details) {
        String message =
                messageArguments == null || messageArguments.length == 0
                        ? messageResolver.resolve(errorCode, locale)
                        : messageResolver.resolve(errorCode, locale, messageArguments);
        return new ApiError(
                errorCode.getCode(),
                message,
                currentTraceId(),
                LocalDateTime.now(),
                request.getRequestURI(),
                fieldErrors == null || fieldErrors.isEmpty() ? null : List.copyOf(fieldErrors),
                details == null || details.isEmpty() ? null : Map.copyOf(details));
    }

    private String currentTraceId() {
        return traceContext.currentTraceId();
    }
}
