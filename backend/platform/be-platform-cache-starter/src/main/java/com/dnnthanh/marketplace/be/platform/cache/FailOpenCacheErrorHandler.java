package com.dnnthanh.marketplace.be.platform.cache;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;

/** Keeps Redis/cache failures from becoming authoritative business-data failures. */
@Slf4j
public final class FailOpenCacheErrorHandler implements CacheErrorHandler {

    /** Logs a cache read failure and lets the underlying use case execute normally. */
    @Override
    public void handleCacheGetError(
            @NonNull RuntimeException exception, @NonNull Cache cache, @NonNull Object key) {
        log("get", exception, cache, key);
    }

    /** Logs a cache write failure without rolling back the source-of-truth transaction. */
    @Override
    public void handleCachePutError(
            @NonNull RuntimeException exception,
            @NonNull Cache cache,
            @NonNull Object key,
            Object value) {
        log("put", exception, cache, key);
    }

    /**
     * Logs a cache eviction failure; short TTL and shared invalidation events repair stale data.
     */
    @Override
    public void handleCacheEvictError(
            @NonNull RuntimeException exception, @NonNull Cache cache, @NonNull Object key) {
        log("evict", exception, cache, key);
    }

    /** Logs a cache clear failure without taking the business API down. */
    @Override
    public void handleCacheClearError(@NonNull RuntimeException exception, @NonNull Cache cache) {
        log("clear", exception, cache, "*");
    }

    private void log(String operation, RuntimeException exception, Cache cache, Object key) {
        log.warn(
                "cache_operation_failed operation={} cache={} keyHash={} failure={}",
                operation,
                cache.getName(),
                "*".equals(key) ? "*" : ObjectsHash.hash(key),
                exception.toString());
    }

    private static final class ObjectsHash {

        private ObjectsHash() {}

        private static int hash(Object value) {
            return java.util.Objects.hashCode(value);
        }
    }
}
