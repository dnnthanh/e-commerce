# Source-Owned Administration Audit

## Problem

Application logs are not an audit trail. Sensitive admin actions such as incident recovery, ignore, settlement approval, and payout completion need an immutable business history even when Kafka/Audit service is temporarily unavailable.

## Selected design

The bounded context that owns the state change writes `AuditRequestedEvent` to its **own transactional Outbox** in the same local database transaction as the business mutation. `be-audit-worker` later appends the immutable event into the Audit PostgreSQL store.

Current examples:

- Operations: `OPERATIONS_RECOVERY_REQUESTED`, `OPERATIONS_INCIDENT_IGNORED`.
- Settlement: `SETTLEMENT_APPROVED`, `SETTLEMENT_PAYOUT_COMPLETED`.
- Seller: `SELLER_PROFILE_UPDATED`.

## Why not call be-audit-api synchronously?

A temporary Audit outage must not make a finance approval or recovery action ambiguous. Source Outbox keeps the audit intent durable and isolates Audit throughput from the source API.

## Audit vs log vs notification

- Log: runtime/debug telemetry.
- Audit: who changed what, before/after, actor type, trace id, and source service.
- Notification: who needs to be informed about a business fact.
