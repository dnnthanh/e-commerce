# Use-case learning catalog

Production-relevant cases discovered during implementation are recorded here as required by `AGENTS.MD`. Each note should capture the problem, constraints, alternatives, selected design, failure modes and verification path.

- [`audit-history`](./audit-history/README.md) — Use Case — Immutable Admin Audit History
- [`authorization-durable-provider-mutation-recovery`](./authorization-durable-provider-mutation-recovery/README.md) — Use Case — Durable authorization provider mutation and recovery
- [`cart-authoritative-checkout-validation`](./cart-authoritative-checkout-validation/README.md) — Use Case — Cart authoritative checkout revalidation
- [`catalog-product-publish-and-variant`](./catalog-product-publish-and-variant/README.md) — Use Case — Catalog publish, dynamic variants and checkout snapshot
- [`checkout-saga-compensation`](./checkout-saga-compensation/README.md) — Use Case — Checkout Saga compensation and ambiguous payment
- [`comment-thread-moderation-and-reaction`](./comment-thread-moderation-and-reaction/README.md) — Use Case — Mongo comment thread, moderation and concurrent reaction
- [`consumer-remote-enrichment-transaction-boundary`](./consumer-remote-enrichment-transaction-boundary/README.md) — Use case: Remote enrichment outside the database transaction
- [`deployable-isolation`](./deployable-isolation/README.md) — Use Case — Isolate API, Kafka Worker, Job and Outbox Publisher
- [`feature-013-gap-register`](./feature-013-gap-register/README.md) — Feature 013 — Remaining use-case gap register
- [`fulfillment-multi-package-and-reconciliation`](./fulfillment-multi-package-and-reconciliation/README.md) — Use Case — Multi-package fulfillment allocation and reconciliation
- [`inventory-oversell-and-reconciliation`](./inventory-oversell-and-reconciliation/README.md) — Use Case — Inventory reservation, oversell prevention and reconciliation
- [`kafka-operations-vs-monitoring`](./kafka-operations-vs-monitoring/README.md) — Kafka operations UI vs metrics monitoring
- [`local-lgtm-observability`](./local-lgtm-observability/README.md) — Local LGTM bundle vs production observability
- [`media-processing-idempotency-and-safe-delete`](./media-processing-idempotency-and-safe-delete/README.md) — Use Case — Media processing idempotency and reference-safe deletion
- [`media-seed-consistency`](./media-seed-consistency/README.md) — Use case: keeping media metadata and object storage consistent
- [`mongodb-transactional-outbox`](./mongodb-transactional-outbox/README.md) — MongoDB Transactional Outbox for Comment → Notification
- [`notification-delivery-retry-dlq-replay`](./notification-delivery-retry-dlq-replay/README.md) — Use Case — Durable notification delivery, retry, DLQ and replay
- [`notification-disconnect-reconnect`](./notification-disconnect-reconnect/README.md) — Notification disconnect / reconnect
- [`order-lifecycle-and-operations-override`](./order-lifecycle-and-operations-override/README.md) — Use Case — Marketplace Order lifecycle and guarded operations override
- [`payment-unknown-operations-recovery`](./payment-unknown-operations-recovery/README.md) — Payment UNKNOWN → Operations Recovery Loop
- [`payment-webhook-idempotency-and-refund`](./payment-webhook-idempotency-and-refund/README.md) — Use Case — Payment callback idempotency, UNKNOWN recovery and cumulative refund
- [`platform-cache-kafka-abstractions`](./platform-cache-kafka-abstractions/README.md) — Use case: Platform cache and Kafka abstractions
- [`promotion-targeting-and-reservation`](./promotion-targeting-and-reservation/README.md) — Use Case — Promotion targeting, stacking and checkout reservation
- [`return-partial-refund-disposition-and-dispute`](./return-partial-refund-disposition-and-dispute/README.md) — Use Case — Partial return, inspection, dispute and inventory disposition
- [`review-verified-purchase-and-moderation`](./review-verified-purchase-and-moderation/README.md) — Use Case — Verified-purchase Review ownership and moderation
- [`search-stale-event-and-reindex`](./search-stale-event-and-reindex/README.md) — Use Case — Search projection stale-event protection and reindex
- [`seller-change-notification`](./seller-change-notification/README.md) — Use Case — Notify Customers About Material Seller Changes
- [`service-disconnect-recovery`](./service-disconnect-recovery/README.md) — Service disconnect / dependency recovery
- [`settlement-ledger-and-reconciliation`](./settlement-ledger-and-reconciliation/README.md) — Use Case — Immutable seller settlement ledger and reconciliation
- [`source-owned-admin-audit`](./source-owned-admin-audit/README.md) — Source-Owned Administration Audit
- [`typed-redis-object-cache`](./typed-redis-object-cache/README.md) — Typed Redis Object Cache
