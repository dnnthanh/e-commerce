# Checkout Orchestration

## Production use cases
- snapshot cart.
- price quote.
- promotion reserve.
- inventory reserve.
- shipping quote.
- payment initiation.
- exact-once order create.
- resume/recovery.

## Business invariants
- idempotency key maps to one saga.
- no remote HTTP inside DB transaction.
- compensation is duplicate-safe.
- snapshot money immutable.

## Patterns / techniques
- **Saga/Process Manager**: applied only where the use case above needs it.
- **State**: applied only where the use case above needs it.
- **Outbox/Inbox**: applied only where the use case above needs it.
- **Compensation**: applied only where the use case above needs it.

## Persistence and concurrency

Saga state persisted locally; remote calls outside transaction; each side effect uses business idempotency key.

## Failure and recovery

Recover stuck states; release promo/inventory on pre-order failure; reconcile unknown payment.

## Required tests
- happy path.
- failure after each boundary.
- duplicate submit.
- lost response recovery.

## Completion rule

CRUD-only behavior is not sufficient. The context is complete only when the applicable invariants, idempotency/concurrency, failure recovery, observability and fresh verification evidence are implemented.
