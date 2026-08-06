# Audit

## Production use cases
- append-only business audit.
- search by actor/resource/time/action.
- redaction.
- idempotent event ingestion.
- retention.

## Business invariants
- existing audit event cannot be mutated.
- sensitive values redacted.
- duplicate event key does not duplicate audit.

## Patterns / techniques
- **Append-only Ledger**: applied only where the use case above needs it.
- **Idempotent Consumer**: applied only where the use case above needs it.
- **CQRS-lite**: applied only where the use case above needs it.

## Persistence and concurrency

JDBC/native read projection is justified in outbound adapter for audit reporting; partitions exercised in DB lab.

## Failure and recovery

Replay ingestion safely; retention archived according to policy.

## Required tests
- append-only constraint.
- dedupe.
- search pagination.
- redaction.

## Completion rule

CRUD-only behavior is not sufficient. The context is complete only when the applicable invariants, idempotency/concurrency, failure recovery, observability and fresh verification evidence are implemented.
