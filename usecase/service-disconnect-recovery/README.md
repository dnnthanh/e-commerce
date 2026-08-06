# Service disconnect / dependency recovery

## HTTP dependencies
Use strict connect/read timeouts. Retry only transient idempotent operations with exponential backoff + jitter. Circuit breakers fail fast and bulkheads isolate slow dependencies. Ambiguous payment operations become `UNKNOWN` and use query/reconciliation instead of blind retry.

## Kafka and asynchronous work
The source transaction writes Outbox atomically. Kafka downtime leaves Outbox rows pending for `be-<context>-outbox` to publish later. Consumers use Inbox/idempotency plus retry/DLT for failures.

## Jobs/workers
Persistent job runs/checkpoints are the recovery source. Work is chunked and idempotent so another instance can resume after process failure.

## Redis/OpenSearch
Redis cache can fail open to the source DB where business rules permit. OpenSearch outage degrades search only; checkout/order/payment do not depend on search availability.

## Runtime instance failure
Readiness removes unhealthy instances from traffic. Durable state is outside process memory, allowing replacement instances to recover.
