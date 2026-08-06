package com.dnnthanh.marketplace.be.platform.trace;

import org.springframework.boot.restclient.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Adds the current trace id to outbound blocking HTTP requests. */
@Configuration
public class TracePropagationConfiguration {

    @Bean
    public RestClientCustomizer traceIdRestClientCustomizer(TraceContextAccessor traceContext) {
        return builder ->
                builder.requestInterceptor(
                        (request, body, execution) -> {
                            String traceId = traceContext.currentTraceId();
                            if (traceId != null) {
                                request.getHeaders().set(TraceHeaders.TRACE_ID, traceId);
                            }
                            return execution.execute(request, body);
                        });
    }
}
