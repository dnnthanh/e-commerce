# Settlement

## Production use cases
- seller payable ledger.
- period close.
- statement.
- refund adjustment.
- manual adjustment.
- provider reconciliation.
- payout-ready.
- hold/dispute.

## Business invariants
- ledger entries immutable.
- balance rebuild equals materialized total.
- consumed event idempotent.
- closed period not silently mutated.

## Patterns / techniques
- **Ledger**: applied only where the use case above needs it.
- **Inbox/Outbox**: applied only where the use case above needs it.
- **Reconciliation**: applied only where the use case above needs it.
- **CQRS-lite**: applied only where the use case above needs it.

## Persistence and concurrency

Oracle/relational ledger through outbound adapter; application ports only; large-ledger keyset/partition lab.

## Failure and recovery

Post later-period adjustments; reconcile provider/internal differences; replay idempotently.

## Required tests
- ledger rebuild.
- duplicate events.
- refund next period.
- close concurrency.

## Completion rule

CRUD-only behavior is not sufficient. The context is complete only when the applicable invariants, idempotency/concurrency, failure recovery, observability and fresh verification evidence are implemented.
