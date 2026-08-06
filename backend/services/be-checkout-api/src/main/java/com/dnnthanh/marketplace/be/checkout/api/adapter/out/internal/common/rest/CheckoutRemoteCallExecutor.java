package com.dnnthanh.marketplace.be.checkout.api.adapter.out.internal.common.rest;

import com.dnnthanh.marketplace.be.platform.stereotype.Adapter;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;

/** Applies the configured circuit breaker and bulkhead around one remote dependency call. */
@Adapter
@RequiredArgsConstructor
public class CheckoutRemoteCallExecutor {
    private final CircuitBreakerRegistry circuitBreakers;
    private final BulkheadRegistry bulkheads;

    public <T> T execute(String dependencyName, Supplier<T> supplier) {
        CircuitBreaker circuitBreaker = circuitBreakers.circuitBreaker(dependencyName);
        Bulkhead bulkhead = bulkheads.bulkhead(dependencyName);
        Supplier<T> protectedCall = CircuitBreaker.decorateSupplier(circuitBreaker, supplier);
        return Bulkhead.decorateSupplier(bulkhead, protectedCall).get();
    }

    public void execute(String dependencyName, Runnable runnable) {
        execute(
                dependencyName,
                () -> {
                    runnable.run();
                    return null;
                });
    }
}
