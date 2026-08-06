# Return & Refund Orchestration

## Production use cases
- eligibility.
- partial quantity return.
- evidence.
- approve/reject.
- return shipment.
- inspection.
- refund.
- restock/scrap/quarantine.
- dispute.

## Business invariants
- return qty cumulative <= delivered qty.
- refund cumulative <= refundable snapshot.
- refund key idempotent.

## Patterns / techniques
- **Saga**: applied only where the use case above needs it.
- **State**: applied only where the use case above needs it.
- **Policy/Specification**: applied only where the use case above needs it.
- **Inbox/Outbox**: applied only where the use case above needs it.

## Persistence and concurrency

Return aggregate + lines in JPA/JDBC adapter; external order/payment/inventory ports; no remote call inside DB transaction.

## Failure and recovery

Recover refund-success/downstream-failure; inspect/reconcile stuck returns.

## Required tests
- eligibility.
- partial/cumulative return.
- refund recovery.
- state concurrency.

## Completion rule

CRUD-only behavior is not sufficient. The context is complete only when the applicable invariants, idempotency/concurrency, failure recovery, observability and fresh verification evidence are implemented.
