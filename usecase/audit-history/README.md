# Use Case — Immutable Admin Audit History

## Problem
Runtime logs are not a reliable business audit trail. Admin/security/manual recovery actions need searchable history answering who changed what, why, when and from which trace.

## Candidate solutions
1. Only application logs — rejected: retention/query/schema and immutability semantics are weak.
2. Direct synchronous call to central audit service — rejected as a central availability dependency.
3. Source transaction + transactional outbox -> `be-audit` idempotent consumer — selected.

## Practice
Perform a permission change, seller suspension and DLT replay. Stop `be-audit`, commit another protected action, verify source outbox backlog, restart audit, verify records arrive once. Then use the database-performance lab to measure unindexed audit-history search before/after index/partition design.
