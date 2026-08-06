package com.dnnthanh.marketplace.be.platform.cache;

import java.time.Duration;
import java.util.Objects;

/**
 * Defines one typed Redis-backed Spring cache.
 *
 * @param name stable cache name
 * @param ttl maximum lifetime of one cache entry
 * @param valueType concrete object type stored in Redis for this cache
 */
public record RedisCacheSpec(String name, Duration ttl, Class<?> valueType) {

    /** Validates a cache specification before it is used to build the cache manager. */
    public RedisCacheSpec {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(ttl, "ttl");
        Objects.requireNonNull(valueType, "valueType");
        if (name.isBlank()) {
            throw new IllegalArgumentException("cache name must not be blank");
        }
        if (ttl.isZero() || ttl.isNegative()) {
            throw new IllegalArgumentException("cache ttl must be positive");
        }
    }
}
