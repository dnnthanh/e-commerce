# Use case: Platform cache and Kafka abstractions

## Problem

When every bounded context talks directly to Redis or Kafka, infrastructure concerns leak into business code. Typical symptoms are repeated `ObjectMapper` calls, inconsistent TTL/serialization, direct `KafkaTemplate` usage, ad-hoc error logging, and different event filtering rules in every listener.

## Chosen design

The platform owns the common transport concerns while bounded contexts keep their domain ports/adapters.

### Redis

`MarketplaceCacheManager` wraps Spring `CacheManager` and exposes generic typed `get`, `put`, and `evict` operations. `RedisCacheManager` owns value serializers, TTLs, prefixes, transaction awareness, and fail-open behavior. Rich aggregates are mapped to explicit cache documents before they enter Redis.

### Kafka producer

`BaseKafkaProducer<T>` owns `KafkaTemplate<String,Object>` and transport-level publish logging. Concrete producers such as `DomainEventProducer` extend it and expose a domain-friendly API. Bounded-context services do not inject `KafkaTemplate` directly.

### Kafka consumer

`BaseDomainEventConsumer` owns envelope validation, supported-event filtering, and failure logging. Concrete `@KafkaListener` adapters extend it and keep only topic wiring plus business handling. Listeners receive objects through Spring Kafka SerDe; they do not parse raw JSON strings.

## Why inheritance here is acceptable

The base classes implement a stable transport template with a narrow extension point. They do not contain bounded-context business rules. Composition remains preferred for domain behavior; inheritance is limited to the Kafka adapter template where the lifecycle is intentionally uniform.

## Failure behavior

- Redis is cache-aside and fail-open for the configured use cases; the authoritative store remains the database.
- Unknown cache names fail fast because dynamic cache creation is disabled.
- Kafka producer failures are returned through the asynchronous send result so Outbox publishers can retain `PENDING` state.
- Consumer failures are rethrown after common logging so Spring Kafka retry/DLT policy remains authoritative.

## Anti-patterns rejected

- `StringRedisTemplate` for object caches.
- `RedisTemplate<String,String>` followed by manual JSON conversion.
- `ObjectMapper` inside Kafka listeners.
- `KafkaTemplate` injected into bounded-context services.
- One giant platform helper that knows domain-specific topics or business payloads.

## Verification

`verification/verify_platform_abstractions.py` and `verification/verify_typed_redis_cache.py` enforce these boundaries.
