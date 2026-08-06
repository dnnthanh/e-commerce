# Typed Redis Object Cache

## Problem

A common shortcut is to use `StringRedisTemplate`, serialize an object with `ObjectMapper.writeValueAsString`, then deserialize it manually with `readValue`. That leaks infrastructure serialization into business adapters, duplicates configuration, makes type mistakes easy, and encourages inconsistent TTL/key/error policies.

## Chosen design

This project uses Spring Cache through `be-platform-cache-starter`.

```text
Business/domain object
        ↓
cache DTO/document
        ↓
Spring Cache
        ↓
RedisCacheManager
        ↓
typed JacksonJsonRedisSerializer<T>
        ↓
Redis bytes
```

Business code never receives Redis JSON strings.

## Central cache manager

`MarketplaceRedisCacheAutoConfiguration` enables caching and creates a `RedisCacheManager` with:

- an explicit `RedisCacheSpec` for every cache;
- a concrete Java value type per cache;
- explicit TTL per cache;
- deterministic `marketplace::<cache-name>::` key prefixes;
- `transactionAware()` cache writes/evictions;
- runtime creation of undeclared caches disabled;
- cache statistics enabled;
- fail-open cache error handling for caches whose source of truth is elsewhere.

## Cart example

MySQL remains authoritative. Redis stores `CartCacheDocument`, not the rich `Cart` aggregate and not a JSON `String`.

```text
Cart
 ↓ map
CartCacheDocument
 ↓ CacheManager
Redis
```

On cache failure, Cart falls back to MySQL. Checkout still reprices authoritatively and never trusts the cart price snapshot.

## Authorization example

`AuthorizationApplicationService.snapshot(userId)` uses `@Cacheable`. Role/seller-scope mutations use `@CacheEvict`, so a Keycloak authorization change invalidates the shared Redis snapshot immediately. TTL is deliberately short because authorization is security-sensitive.

## Rejected alternatives

### `StringRedisTemplate` + manual JSON conversion

Rejected because every caller becomes responsible for serialization and error handling.

### Serializing rich domain aggregate directly

Rejected for Cart because domain behavior/constructor changes would unnecessarily couple the Redis schema to the aggregate implementation.

### JDK serialization

Rejected because it is opaque, Java-specific, harder to inspect, and more fragile for long-lived cache formats.

## Failure behavior

Redis is declared as `CACHE` for these cases. Therefore a Redis outage degrades performance but does not become business-data loss. This fail-open rule must not be copied to Redis usages where Redis is the authoritative state store, lock, rate limiter, or counter without a separate design decision.
