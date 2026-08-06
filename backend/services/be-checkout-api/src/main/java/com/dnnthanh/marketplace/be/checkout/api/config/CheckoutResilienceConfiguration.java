package com.dnnthanh.marketplace.be.checkout.api.config;

import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

/** Programmatic Resilience4j configuration compatible with the Spring Boot 4 baseline. */
@Configuration
@EnableConfigurationProperties(CheckoutRemoteResilienceProperties.class)
public class CheckoutResilienceConfiguration {

    @Bean
    CircuitBreakerRegistry checkoutCircuitBreakerRegistry(
            CheckoutRemoteResilienceProperties properties) {
        CircuitBreakerConfig config =
                CircuitBreakerConfig.custom()
                        .slidingWindowSize(properties.getSlidingWindowSize())
                        .minimumNumberOfCalls(properties.getMinimumNumberOfCalls())
                        .failureRateThreshold(properties.getFailureRateThreshold())
                        .waitDurationInOpenState(properties.getWaitDurationInOpenState())
                        .permittedNumberOfCallsInHalfOpenState(
                                properties.getPermittedNumberOfCallsInHalfOpenState())
                        .recordExceptions(
                                ResourceAccessException.class, HttpServerErrorException.class)
                        .build();
        return CircuitBreakerRegistry.of(config);
    }

    @Bean
    BulkheadRegistry checkoutBulkheadRegistry(CheckoutRemoteResilienceProperties properties) {
        BulkheadConfig config =
                BulkheadConfig.custom()
                        .maxConcurrentCalls(properties.getMaxConcurrentCalls())
                        .maxWaitDuration(properties.getMaxWaitDuration())
                        .build();
        return BulkheadRegistry.of(config);
    }
}
