# Media Pipeline

## Production use cases
- presigned upload.
- upload state machine.
- scan and metadata extraction.
- variant generation.
- checksum dedupe.
- retry/DLQ/replay.
- safe delayed delete.

## Business invariants
- same media/checksum processing is idempotent.
- public URL independent of object-store path.
- referenced media is not physically deleted.

## Patterns / techniques
- **State**: applied only where the use case above needs it.
- **Adapter**: applied only where the use case above needs it.
- **Idempotent Consumer**: applied only where the use case above needs it.
- **Outbox**: applied only where the use case above needs it.

## Persistence and concurrency

Metadata in relational store; binary in object storage; worker claims with idempotency key.

## Failure and recovery

Retry only retryable transformations, keep FAILED reason, replay via operations.

## Required tests
- state transition tests.
- checksum duplicate test.
- worker retry/replay integration.

## Completion rule

CRUD-only behavior is not sufficient. The context is complete only when the applicable invariants, idempotency/concurrency, failure recovery, observability and fresh verification evidence are implemented.
