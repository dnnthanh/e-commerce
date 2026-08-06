package com.dnnthanh.marketplace.be.platform.cache;

import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

/**
 * Generic typed cache facade used by adapters that need imperative cache access. Serialization,
 * TTL, prefixes, and Redis details remain owned by the platform cache starter.
 */
@Slf4j
@RequiredArgsConstructor
public final class MarketplaceCacheManager {

    private final CacheManager delegate;

    /**
     * Reads a typed cache value using fail-open semantics.
     *
     * @param cacheName configured cache name
     * @param key cache key
     * @param valueType expected cached value type
     * @param <T> cached value type
     * @return cached object or empty when missing/unavailable
     */
    public <T> Optional<T> get(String cacheName, Object key, Class<T> valueType) {
        Objects.requireNonNull(valueType, "valueType");
        try {
            return Optional.ofNullable(cache(cacheName).get(key, valueType));
        } catch (RuntimeException failure) {
            log.warn(
                    "cache_read_failed cache={} keyHash={} valueType={} failure={}",
                    cacheName,
                    Objects.hashCode(key),
                    valueType.getSimpleName(),
                    failure.toString());
            return Optional.empty();
        }
    }

    /**
     * Stores a typed cache value using the serializer configured for the named cache.
     *
     * @param cacheName configured cache name
     * @param key cache key
     * @param value typed cache value
     * @param <T> cached value type
     */
    public <T> void put(String cacheName, Object key, T value) {
        try {
            cache(cacheName).put(key, value);
        } catch (RuntimeException failure) {
            log.warn(
                    "cache_write_failed cache={} keyHash={} valueType={} failure={}",
                    cacheName,
                    Objects.hashCode(key),
                    value == null ? "null" : value.getClass().getSimpleName(),
                    failure.toString());
        }
    }

    /**
     * Evicts one cache key without failing the authoritative business operation when Redis is
     * unavailable.
     *
     * @param cacheName configured cache name
     * @param key cache key
     */
    public void evict(String cacheName, Object key) {
        try {
            cache(cacheName).evict(key);
        } catch (RuntimeException failure) {
            log.warn(
                    "cache_evict_failed cache={} keyHash={} failure={}",
                    cacheName,
                    Objects.hashCode(key),
                    failure.toString());
        }
    }

    private Cache cache(String cacheName) {
        return Objects.requireNonNull(
                delegate.getCache(cacheName), "cache is not configured: " + cacheName);
    }
}
