# Order

## Production use cases
- marketplace parent + seller orders.
- immutable line/address/payment snapshot.
- customer/seller/admin queries.
- cancellation policy.
- partial cancellation.
- payment/fulfillment events.
- unpaid expiry.
- timeline/override.

## Business invariants
- illegal transitions rejected.
- checkout key creates one order.
- event side effects duplicate-safe.
- snapshot never follows mutable catalog/price.

## Patterns / techniques
- **Aggregate**: applied only where the use case above needs it.
- **State**: applied only where the use case above needs it.
- **Specification**: applied only where the use case above needs it.
- **CQRS-lite**: applied only where the use case above needs it.
- **Inbox/Outbox**: applied only where the use case above needs it.
- **Optimistic Locking**: applied only where the use case above needs it.

## Persistence and concurrency

Spring Data JPA for aggregate, Specification/projection reads, @Version, inbox/outbox same local transaction.

## Failure and recovery

Auto-cancel/reconcile stuck orders; reject stale transitions; operations override audited.

## Required tests
- state machine.
- idempotent create/event.
- concurrent cancel/payment.
- search projection.

## Completion rule

CRUD-only behavior is not sufficient. The context is complete only when the applicable invariants, idempotency/concurrency, failure recovery, observability and fresh verification evidence are implemented.
