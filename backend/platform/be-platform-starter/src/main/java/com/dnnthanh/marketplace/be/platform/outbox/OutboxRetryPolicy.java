package com.dnnthanh.marketplace.be.platform.outbox;

import java.time.Duration;
import java.util.Objects;

/** Exponential retry policy shared by outbox publishers after a broker delivery failure. */
public record OutboxRetryPolicy(int maxAttempts, Duration baseDelay, Duration maxDelay) {

    public OutboxRetryPolicy {
        if (maxAttempts <= 0) {
            throw new IllegalArgumentException("maxAttempts must be positive");
        }
        Objects.requireNonNull(baseDelay, "baseDelay");
        Objects.requireNonNull(maxDelay, "maxDelay");
        if (baseDelay.isZero() || baseDelay.isNegative()) {
            throw new IllegalArgumentException("baseDelay must be positive");
        }
        if (maxDelay.compareTo(baseDelay) < 0) {
            throw new IllegalArgumentException(
                    "maxDelay must be greater than or equal to baseDelay");
        }
    }

    /** Production default: ten delivery attempts with capped exponential backoff. */
    public static OutboxRetryPolicy defaults() {
        return new OutboxRetryPolicy(10, Duration.ofSeconds(2), Duration.ofMinutes(5));
    }

    /** Returns the delay before the next attempt, capped at {@link #maxDelay()}. */
    public Duration nextDelay(int attemptCount) {
        if (attemptCount <= 1) {
            return baseDelay;
        }
        Duration delay = baseDelay;
        for (int attempt = 1; attempt < attemptCount; attempt++) {
            if (delay.compareTo(maxDelay) >= 0) {
                return maxDelay;
            }
            try {
                Duration doubled = delay.multipliedBy(2);
                delay = doubled.compareTo(maxDelay) > 0 ? maxDelay : doubled;
            } catch (ArithmeticException overflow) {
                return maxDelay;
            }
        }
        return delay;
    }

    /** Returns whether the current failed attempt must move to the terminal FAILED state. */
    public boolean terminal(int attemptCount) {
        return attemptCount >= maxAttempts;
    }
}
