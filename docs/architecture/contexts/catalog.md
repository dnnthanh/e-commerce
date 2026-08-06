# Catalog & Dynamic Attributes

## Production use cases
- SPU/SKU lifecycle.
- category re-parent with cycle prevention.
- typed dynamic attributes.
- SKU-combination generation.
- draft/review/publish/archive.
- bulk import with item-level result.
- versioned product content.

## Business invariants
- Product state transitions are legal.
- variant combination is unique.
- published products have required attributes/media.
- referenced attribute values cannot be removed silently.

## Patterns / techniques
- **Aggregate**: applied only where the use case above needs it.
- **Strategy**: applied only where the use case above needs it.
- **Factory**: applied only where the use case above needs it.
- **Specification**: applied only where the use case above needs it.
- **Outbox**: applied only where the use case above needs it.

## Persistence and concurrency

Spring Data JPA for aggregates; projections/entity graphs for reads; optimistic @Version; outbox for publish/reindex.

## Failure and recovery

Reject illegal transitions/duplicate variants; resume bulk jobs; rebuild search projection from source.

## Required tests
- domain state/attribute tests.
- repository constraints.
- concurrent edit test.
- outbox/reindex integration.

## Completion rule

CRUD-only behavior is not sufficient. The context is complete only when the applicable invariants, idempotency/concurrency, failure recovery, observability and fresh verification evidence are implemented.
