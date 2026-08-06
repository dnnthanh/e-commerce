# Feature Spec 012 — Immutable Audit History

## Goal

Provide searchable, immutable history for protected administrative, security, seller-moderation and manual recovery operations.

## Deployables

- `be-audit-api`: permission-protected paginated search/detail for admin/operations investigation.
- `be-audit-worker`: idempotent Kafka ingestion and append-only persistence.

Together they form the logical `be-audit` bounded context. `be-audit-worker` stores append-only audit records in PostgreSQL. Source services publish audit events through their own transactional outbox so an audit outage cannot silently lose a committed protected action.

## Required audit fields

- eventId;
- action;
- resourceType/resourceId;
- actorType/actorId;
- sourceService;
- traceId/correlationId;
- `LocalDateTime occurredAt`;
- outcome/result;
- required reason for manual sensitive actions;
- safe before/after change summary.

Never store access/refresh tokens, passwords, secrets, signatures, raw payment credentials or other masked fields.

## Required actions

Audit at least:

- Keycloak role/permission/seller-scope administration;
- seller verification/suspension/material-profile changes;
- review/comment moderation;
- promotion/admin changes;
- manual DLT replay/retry/resolve;
- stuck-Saga recovery/compensation action;
- payment reconciliation/manual resolution/refund admin action;
- settlement approval/manual finance operation.

## Admin UI

Provide permission-protected paginated search/detail by actor, action, resource, source service, outcome and date range. Normal APIs cannot edit/delete history.

## Scale learning

Baseline table starts without deliberate performance indexes/partition beyond integrity constraints. Database lab later captures an unoptimized audit-history query plan, then compares index and partition strategies (including PostgreSQL `pg_partman`) with saved evidence.
