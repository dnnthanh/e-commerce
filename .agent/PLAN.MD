# Marketplace Master Execution Plan

## Goal

Deliver a complete, polished, production-oriented multi-seller marketplace that starts locally with one canonical command:

```bash
docker compose up -d --build
```

The finished repository must include a realistic Angular storefront/admin experience, polyglot persistence, Keycloak security, Kafka event flows, realtime notifications, observability, reliability/recovery mechanisms, Docker/Kubernetes assets, substantial seed data, and reproducible verification evidence.

## Delivery branch model

```text
master
  ^
develop                 # core/integration
  ^
feature/*               # one reviewed feature/spec at a time
```

## Phase sequence

### Phase 0 — Architecture and agent governance

Deliverables:
- `AGENTS.MD`
- `.agent/PLAN.MD`
- `.agent/CONVENTIONS.MD`
- production architecture specification
- service/data ownership map
- initial ADRs

Exit gate:
- architecture reviewed by user
- no business implementation before approval

### Phase 1 — Platform foundation

Feature specs:
- build/BOM conventions
- shared technical libraries only
- error/i18n contract
- HTTP request/response structured logging + masking
- global trim + opt-out
- trace context
- Kafka typed SerDe/event envelope
- scheduler execution context
- test foundation
- Docker image conventions

### Phase 2 — Identity, authorization and user context

Feature specs:
- Keycloak realm/client/service-account bootstrap
- role/permission tree
- seller-scoped authorization
- `/private/me/authorization`
- `@PreAuthorize` authorization bean
- request-scoped `UserContext`
- system/service contexts for Kafka/scheduler
- authorization caching/invalidation
- Angular auth/permission store
- authorization DOCX handbook

### Phase 3 — Runtime platform and observability

Feature specs:
- API Gateway using WebFlux/Spring Cloud Gateway
- OpenTelemetry Collector
- Prometheus/Grafana
- Loki
- Tempo or Jaeger-compatible tracing backend
- Kafka UI
- CloudBeaver database UI
- MinIO UI
- health/readiness/startup probes
- graceful shutdown

### Phase 4 — Seller/shop and catalog

Feature specs:
- seller/shop service (MySQL)
- catalog service (PostgreSQL)
- category/brand/product/SKU/dynamic attributes
- seller ownership and authorization
- realistic seed generation
- public product/category/shop APIs
- private seller/admin APIs

### Phase 5 — Media and search/discovery

Feature specs:
- MinIO-backed media pipeline
- async processing pipeline
- search index/read model
- autocomplete/filter/sort/ranking
- public search API
- database/query comparison lab

### Phase 6 — Pricing and promotion

Feature specs:
- pricing rules/temporal price
- channel/seller/campaign price
- promotion eligibility/rules/stacking
- Strategy/Specification/Composite decisions
- cache strategy and invalidation

### Phase 7 — Inventory/reservation and cart

Feature specs:
- inventory ledger and stock model
- reservation/expiry/idempotency
- concurrency control and oversell prevention
- cart persistence/cache behavior
- guest/authenticated cart and merge
- repricing on checkout

### Phase 8 — Checkout and order

Feature specs:
- multi-seller parent order / seller orders
- checkout orchestration
- persistent Saga/process manager
- compensation/recovery
- order lifecycle/state machine
- SQL Server order persistence

### Phase 9 — Payment, refund and finance settlement

Feature specs:
- payment provider port/adapters
- mock provider for local Compose
- idempotency and webhook Inbox
- unknown outcome + reconciliation
- refund
- commission/fees/settlement
- Oracle finance/settlement persistence

### Phase 10 — Fulfillment/shipping

Feature specs:
- warehouse allocation strategy
- shipment splitting
- shipping provider adapter
- fulfillment state machine
- SQL Server persistence

### Phase 11 — Comment, review, notification and realtime

Feature specs:
- product Q&A/comment threads (MongoDB)
- review/rating/moderation
- notification source-of-truth (MongoDB)
- unread counters/cache
- realtime delivery to Angular
- Kafka/system-context/internal-auth integration tests

### Phase 12 — Return/refund and operations center

Feature specs:
- return eligibility/workflow
- partial return/refund
- restock decision
- failed-event/DLT operations
- stuck Saga management
- payment reconciliation
- scheduler/job-run visibility
- replay/retry/manual resolution

### Phase 13 — Database performance learning labs

Feature specs:
- PostgreSQL EXPLAIN/ANALYZE
- B-tree/composite/partial/covering/GIN/BRIN examples
- pagination offset vs keyset
- PostgreSQL `pg_partman` partition lab
- Oracle execution plan/SQL examples
- SQL Server clustered/nonclustered/include/deadlock labs
- MySQL/MariaDB explain/composite/covering/locking labs

Important: these labs do not justify unnecessary production indexes/partitions in baseline business schema.

### Phase 14 — Kubernetes/Helm

Feature specs:
- generic Kubernetes manifests/Helm
- Deployments/Services/Ingress or Gateway API
- ConfigMap/Secret wiring
- probes/resource requests/limits
- HPA/PDB
- NetworkPolicy/service accounts
- rolling update and graceful shutdown

### Phase 15 — Verification, UX polish and final delivery

Required evidence:
- backend build/tests
- integration/concurrency/resilience results
- Docker Compose full-stack health
- Angular build
- screenshot set for anonymous/customer/seller/admin flows
- realtime notification screenshots
- permission-denied/error/i18n screenshots
- operations/recovery screenshots
- test summary and architecture quality report
- security/authorization DOCX
- failure/recovery DOCX
- final ZIP snapshot

## Per-feature quality gate

Every feature must pass:

1. Approved spec.
2. Implementation plan.
3. Backend compile.
4. Unit tests.
5. Applicable integration tests.
6. Applicable architecture/contract/concurrency/resilience tests.
7. Static/architecture quality checks.
8. Docker image build if feature is deployable.
9. Documentation/ADR updates.
10. Fresh evidence stored in `.agent/reports/`.

## Completion standard

A feature is not complete because endpoints return HTTP 200. It is complete only when its happy path, important failure modes, recovery behavior, authorization, observability, and acceptance criteria have been exercised and evidenced.
