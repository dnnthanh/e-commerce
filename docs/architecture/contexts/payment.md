# Payment & Refund

## Production use cases
- payment intent/attempts.
- provider strategy.
- verified webhooks.
- unknown outcome reconciliation.
- capture.
- partial refund.
- refund ledger.
- DLQ/replay.

## Business invariants
- one successful capture effect per payment.
- refund total <= captured.
- webhook duplicate-safe.
- unknown result is not assumed failed.

## Patterns / techniques
- **Strategy**: applied only where the use case above needs it.
- **Adapter**: applied only where the use case above needs it.
- **State**: applied only where the use case above needs it.
- **Inbox/Outbox**: applied only where the use case above needs it.
- **Reconciliation**: applied only where the use case above needs it.

## Persistence and concurrency

Relational ledger; provider refs unique; application depends ports, JDBC confined to adapter; no provider call inside transaction.

## Failure and recovery

Query provider for UNKNOWN; idempotent webhook/refund; reconcile settlement/provider records.

## Required tests
- callback race.
- duplicate webhook.
- partial refund cumulative.
- unknown reconciliation.

## Completion rule

CRUD-only behavior is not sufficient. The context is complete only when the applicable invariants, idempotency/concurrency, failure recovery, observability and fresh verification evidence are implemented.
