# Cart

## Production use cases
- guest/user carts.
- merge on login.
- line mutation.
- selection.
- price/stock validation.
- coupon preview.
- save for later.
- expiration.

## Business invariants
- lost updates prevented by version.
- same mutation key is idempotent.
- checkout never trusts cart price snapshot.

## Patterns / techniques
- **Aggregate**: applied only where the use case above needs it.
- **Optimistic Locking**: applied only where the use case above needs it.
- **Cache-aside**: applied only where the use case above needs it.

## Persistence and concurrency

Durable cart semantics explicit; Redis only for cache/ephemeral acceleration; version checked on mutation.

## Failure and recovery

Refresh stale cart facts, report invalid lines, deterministic merge conflicts.

## Required tests
- merge policy.
- multi-tab version conflict.
- idempotent mutation.
- stale-price validation.

## Completion rule

CRUD-only behavior is not sufficient. The context is complete only when the applicable invariants, idempotency/concurrency, failure recovery, observability and fresh verification evidence are implemented.
