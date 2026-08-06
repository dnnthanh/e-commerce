package com.dnnthanh.marketplace.be.checkout.api.config;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Typed resilience settings shared by Checkout remote dependency adapters. */
@Getter
@Setter
@ConfigurationProperties(prefix = "checkout.remote-resilience")
public class CheckoutRemoteResilienceProperties {
    private int slidingWindowSize = 20;
    private int minimumNumberOfCalls = 10;
    private float failureRateThreshold = 50.0F;
    private Duration waitDurationInOpenState = Duration.ofSeconds(10);
    private int permittedNumberOfCallsInHalfOpenState = 3;
    private int maxConcurrentCalls = 30;
    private Duration maxWaitDuration = Duration.ZERO;
}
