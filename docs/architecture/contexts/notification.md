# Notification

## Production use cases
- preferences.
- durable inbox.
- email/push/SMS.
- localized templates.
- dedupe.
- retry/DLQ.
- read/unread.
- quiet hours.
- realtime push.

## Business invariants
- same business event/channel not delivered twice unintentionally.
- durable inbox is source of truth.
- opt-out/quiet-hour respected.

## Patterns / techniques
- **Strategy**: applied only where the use case above needs it.
- **Adapter**: applied only where the use case above needs it.
- **Idempotent Consumer**: applied only where the use case above needs it.
- **Outbox**: applied only where the use case above needs it.

## Persistence and concurrency

Mongo/durable notification store; provider attempts recorded; websocket/SSE only acceleration.

## Failure and recovery

Backoff retry provider failures; DLQ/replay; mark permanent failures without infinite retry.

## Required tests
- dedupe.
- preference/quiet hours.
- provider retry.
- eventual delivery with Awaitility.

## Completion rule

CRUD-only behavior is not sufficient. The context is complete only when the applicable invariants, idempotency/concurrency, failure recovery, observability and fresh verification evidence are implemented.
