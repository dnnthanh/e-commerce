# Inventory & Reservation

## Production use cases
- stock balance.
- atomic reserve.
- confirm/release.
- TTL expiry.
- transfer/adjustment.
- ledger/reconciliation.
- bulk sync.
- low-stock event.

## Business invariants
- available never negative.
- successful reservation count <= stock.
- business key is idempotent.
- ledger reconciles to balance.

## Patterns / techniques
- **Reservation**: applied only where the use case above needs it.
- **Atomic Update**: applied only where the use case above needs it.
- **Inbox/Outbox**: applied only where the use case above needs it.
- **Saga Compensation**: applied only where the use case above needs it.

## Persistence and concurrency

JDBC/native SQL is intentional at contention boundary; atomic conditional updates/row locks; ledger partition lab.

## Failure and recovery

Expire/release reservations; dedupe/out-of-order events; reconciliation repairs diagnosable drift.

## Required tests
- oversell concurrency.
- idempotent reserve.
- expiry/release.
- ledger reconciliation.

## Completion rule

CRUD-only behavior is not sufficient. The context is complete only when the applicable invariants, idempotency/concurrency, failure recovery, observability and fresh verification evidence are implemented.
