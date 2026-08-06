# Pricing

## Production use cases
- base/seller/channel price.
- scheduled price.
- priority resolution.
- price history.
- bulk update.
- checkout quote snapshot.

## Business invariants
- money uses BigDecimal.
- effective price resolution deterministic.
- expired/future rules not selected.

## Patterns / techniques
- **Strategy**: applied only where the use case above needs it.
- **Specification**: applied only where the use case above needs it.
- **Value Object**: applied only where the use case above needs it.
- **Outbox**: applied only where the use case above needs it.

## Persistence and concurrency

JPA for rules/history; optimistic locking; quote snapshots immutable downstream.

## Failure and recovery

Reject overlapping ambiguous rule where priority cannot resolve; replay price-change event.

## Required tests
- priority/timing unit tests.
- concurrent price edit.
- quote immutability.

## Completion rule

CRUD-only behavior is not sufficient. The context is complete only when the applicable invariants, idempotency/concurrency, failure recovery, observability and fresh verification evidence are implemented.
