package com.dnnthanh.marketplace.be.platform.trace;

import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/** Safe access to the current trace context without making tracing a hard runtime dependency. */
@Component
@RequiredArgsConstructor
public class TraceContextAccessor {

    private final ObjectProvider<Tracer> tracerProvider;

    public String currentTraceId() {
        Tracer tracer = tracerProvider.getIfAvailable();
        if (tracer == null) {
            return null;
        }
        var currentSpan = tracer.currentSpan();
        return currentSpan == null ? null : currentSpan.context().traceId();
    }
}
