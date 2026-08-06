# Feature 013 — Remaining use-case gap register

This register is deliberately separate from the implemented learning notes. It prevents a large source tree from being mistaken for full spec completion. The following secondary/advanced requirements from Spec 013 still need production implementation or runtime proof; no thin placeholder classes should be added just to make this list disappear.

## Catalog
- Category tree management with cycle-safe re-parenting.
- Brand lifecycle and seller-listing overlay.
- Resumable bulk catalog import with row-level result.

## Search
- Autocomplete/typo strategy and an executable full-reindex alias-swap workflow (current worker covers incremental projection and stale-event protection).

## Pricing / Promotion
- Pricing bulk update with partial validation/history.
- Tiered and shipping-benefit promotion strategies plus audited manual override.

## Cart / Checkout
- Durable cart expiration policy and explicit coupon-preview API.
- Checkout address validation and shipping-quote selection are not yet modeled as first-class ports/snapshots.

## Order / Payment / Fulfillment
- Order timeline/notes/tags and explicit reconciliation diagnostic endpoint.
- Provider webhook signature verification is not yet implemented for real providers; the current adapters are sandbox/local.
- Fulfillment shipping-quote abstraction and carrier signature verification remain provider-integration work.

## Return / Review / Comment
- Return evidence/media references and explicit audit timeline projection.
- Review seller response, report queue, and anti-spam/rate-limit policy.
- Comment blocked-user policy and hot-thread cursor path.

## Seller / Authorization
- Seller staff/member management, warehouse/pickup addresses, operational settings and dashboard projection.
- Abuse-rate limiting for privileged authorization mutation endpoints.

## Settlement / Operations
- Seller statement/payout-reference use case.
- Operations DLQ inspection, kill switches and durable bulk-admin job model.

## Runtime acceptance still pending
- Java 25 reactor `mvn clean verify`.
- Testcontainers for PostgreSQL/MySQL/SQL Server/Oracle/Mongo/Redis/Kafka where applicable.
- Docker Compose startup, representative HTTP smoke tests and the full mandatory cross-context workflow.

These gaps are intentionally visible. Feature 013 must not be marked fully complete until either implemented and tested or explicitly removed from the approved spec.
