# Operations

## Production use cases
- stuck workflow search.
- idempotent retry/replay.
- DLQ inspection.
- reconciliation commands.
- guarded override.
- kill switches.
- bulk jobs.

## Business invariants
- no raw fix-row endpoint.
- every privileged mutation audited.
- recovery command idempotent.

## Patterns / techniques
- **Command**: applied only where the use case above needs it.
- **Outbox**: applied only where the use case above needs it.
- **CQRS-lite**: applied only where the use case above needs it.
- **Bulk Job**: applied only where the use case above needs it.

## Persistence and concurrency

Read/report SQL in outbound adapter; recovery request persisted with audit/outbox atomically.

## Failure and recovery

Retry only idempotent operations; expose last error/status; replay with reason/operator.

## Required tests
- recovery idempotency.
- audit emission.
- bulk partial failure.
- stuck workflow search.

## Completion rule

CRUD-only behavior is not sufficient. The context is complete only when the applicable invariants, idempotency/concurrency, failure recovery, observability and fresh verification evidence are implemented.
