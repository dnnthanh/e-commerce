# Production Multi-Seller Marketplace — Architecture Design

**Date:** 2026-08-02  
**Status:** Review gate before implementation  
**Target:** Production-oriented reference implementation + realistic local demo + learning labs

## 1. Product vision

Build a complete multi-seller marketplace inspired by the functional depth of modern large marketplaces, without copying another company's branding or UI. The system must be usable as an end-to-end local product, not a CRUD sample.

The canonical developer experience is:

```bash
docker compose up -d --build
```

After startup, a developer can:

- browse a populated Angular storefront;
- search/filter/sort products;
- inspect realistic product variants and dynamic attributes;
- authenticate through Keycloak;
- use cart/checkout/payment/order/return flows;
- use seller and admin features;
- receive realtime notifications;
- inspect Kafka, databases, object storage, logs, metrics, and traces through browser UIs;
- trigger and inspect recovery scenarios;
- read code/specs/ADRs explaining design choices;
- inspect test reports and screenshots proving the stack was actually exercised.

## 2. Non-goals

- Do not use patterns merely to maximize pattern count.
- Do not make every service reactive.
- Do not create premature production indexes/partitions simply because the project includes performance labs.
- Do not share business domain/entity jars across services.
- Do not use a single shared database schema across microservices.
- Do not put every permission/resource assignment into JWT.
- Do not make realtime transport the only source of notification truth.

## 3. Technology baseline

Version baselines are pinned and periodically revalidated before implementation milestones.

- Java 25 LTS.
- Spring Boot 4.1.x. Checkout uses Resilience4j core modules programmatically instead of the Spring-Boot-3-specific starter.
- Angular 22.x with its supported Node/TypeScript range.
- Keycloak 26.7.x.
- Apache Kafka 4.3.1 baseline.
- MongoDB 8.2.x patch line for comment/notification.
- PostgreSQL current supported release selected at implementation lock; `pg_partman` available for the partition lab.
- MySQL current supported GA/LTS line selected at implementation lock.
- MariaDB current supported LTS line selected at implementation lock.
- SQL Server 2025 Linux container for local development/testing, with architecture compatibility documented.
- Oracle AI Database Free container for local finance/settlement learning; production notes explicitly distinguish the free image from supported commercial production deployment.
- Redis for cache/counters/selected transient state.
- OpenSearch/Elasticsearch-compatible search engine for product discovery read model.
- MinIO for object storage/media.
- OpenTelemetry + Prometheus + Grafana + Loki + Tempo/Jaeger-compatible tracing.
- Docker Compose as canonical local bootstrap.
- Generic Kubernetes + Helm for deployment portability.

## 4. Architectural style

### 4.1 Microservices and bounded contexts

The system is microservice-based, with each service owning its business data and schema. Service boundaries follow business capabilities rather than technical layers.

### 4.2 Internal layering

Each service follows a pragmatic Clean/Hexagonal structure:

```text
Inbound adapter
  -> Application use case
    -> Rich domain model / domain service
      -> Port
        -> Outbound adapter
          -> persistence/external/Kafka
```

Controller request data is mapped to application/domain-facing models. Use cases enrich models through required ports and domain rules. Persistence adapters map domain models to persistence entities and invoke repositories/JDBC.

### 4.3 Rich/enriched domain model

Domain models contain behavior and protect invariants when the bounded context benefits from it. Avoid anemic entity setter orchestration in large service classes.

Examples:

- Product controls publishability based on required variants/attributes.
- Inventory reservation controls RESERVED/CONFIRMED/EXPIRED transitions.
- Order controls lifecycle transitions.
- Payment controls pending/unknown/succeeded/failed/refunded transitions.
- Return controls eligibility/state transitions.

Value objects are introduced only where they provide real semantic/invariant value. Search/filter IDs remain simple `Long` values unless richer behavior is necessary.

## 5. Service map and data ownership

| Service | Primary responsibility | Primary storage | Important secondary dependencies |
|---|---|---|---|
| `api-gateway` | routing, edge auth integration, rate limiting, trace propagation | none | Keycloak, Redis |
| `identity-access-service` | effective authorization API, permission/seller-scope resolution/cache | PostgreSQL or dedicated auth metadata store | Keycloak, Redis, Kafka |
| `seller-service` | seller/shop profile, membership/business metadata | MySQL | Kafka |
| `catalog-service` | category, brand, product, SKU, dynamic attributes | PostgreSQL | Redis, Kafka |
| `media-service` | upload metadata, media processing orchestration | PostgreSQL + MinIO | Kafka/work queue if justified |
| `search-service` | search/discovery read model and APIs | OpenSearch | Kafka, optional Redis |
| `pricing-service` | price books, temporal/channel/seller pricing | PostgreSQL | Redis, Kafka |
| `promotion-service` | campaigns, vouchers, eligibility/stacking rules | PostgreSQL | Redis, Kafka |
| `inventory-service` | stock, ledger, reservation, expiry | PostgreSQL | Redis where role is explicit, Kafka |
| `cart-service` | guest/customer cart, merge, validation snapshot | MySQL + Redis | pricing/promotion/inventory APIs |
| `checkout-service` | checkout orchestration and persistent Saga/process state | PostgreSQL | inventory/order/payment APIs/events |
| `order-service` | parent order + seller orders + lifecycle | SQL Server | Kafka |
| `payment-service` | payment attempts, idempotency, provider interaction, refund/reconciliation | PostgreSQL | external provider, Kafka |
| `settlement-service` | commission, seller payable, settlement lifecycle | Oracle | Kafka |
| `fulfillment-service` | allocation, shipment split, carrier state | SQL Server | provider APIs, Kafka |
| `comment-service` | product Q&A, threaded replies, moderation state | MongoDB | Redis optional, Kafka |
| `review-service` | verified-purchase rating/review/moderation | MariaDB | order verification, Kafka |
| `notification-service` | persisted notification inbox, channel delivery, realtime | MongoDB | Redis, Kafka, Keycloak client creds |
| `return-service` | return workflow/inspection/refund coordination | PostgreSQL | order/payment/inventory APIs/events |
| `operations-service` | DLT/replay/reconciliation/stuck workflow operational views | PostgreSQL | Kafka + service admin APIs |

Local Docker Compose may run multiple logical PostgreSQL databases in one PostgreSQL container while preserving independent databases/users/migrations. This is a local physical optimization only; ownership remains service-specific.

## 6. API conventions

### 6.1 Interface-driven controllers

All Spring MVC/WebFlux HTTP controllers use interface-driven contracts following the pattern supported by Spring request-mapping annotations on interfaces.

- Mapping/OpenAPI metadata lives on the API interface.
- Implementation delegates to use cases and injects MapStruct mappers as Spring beans.
- No `Mapper.INSTANCE` static access.
- Every interface method has meaningful JavaDoc.
- Every class field has meaningful JavaDoc; record components are documented with `@param` entries.

### 6.2 Public/private routing

Public APIs use resource-first routes, e.g.:

```text
/products
/products/{id}
/categories
/shops
/search
/promotions
```

Private/authenticated APIs live under:

```text
/private/products
/private/orders
/private/cart
/private/payments
/private/returns
/private/seller/...
/private/admin/...
/private/me/authorization
```

Do not prepend `/api/v1` by default. Contract evolution uses compatibility/versioning only when a real breaking-version need appears.

### 6.3 Search/query API design

- Use Criteria/Command/Context records instead of 8–20 method parameters.
- Use plain `Long` IDs in search criteria unless a richer type protects a real invariant.
- Pageable endpoints use Spring `Pageable`.
- Simple queries use Spring Data derived queries.
- Moderate dynamic filters may use Specification.
- Complex SQL uses JDBC (`JdbcClient`/`NamedParameterJdbcTemplate`) with explicit SQL, mapping, and count query/page assembly.
- Database-specific learning queries stay in dedicated labs or adapters rather than leaking into domain/application logic.

## 7. Authentication and authorization

### 7.1 Authentication

Keycloak provides OIDC authentication.

- Angular uses Authorization Code + PKCE.
- Backend services are OAuth2 resource servers where applicable.
- Internal service-to-service calls use client credentials/service identity when auth is required.

### 7.2 Permission model

Roles aggregate permissions. Backend authorization checks permissions, not role names.

Example hierarchy:

```text
Role
  -> permissions[]
User
  -> roles[]
  -> seller/resource scopes[]
```

Seller-specific permissions are supported so one user can manage selected sellers/shops with different effective capabilities.

### 7.3 JWT size

JWT carries only authentication and essential compact claims. It does not embed hundreds of permissions or seller IDs.

### 7.4 Authorization API

After login Angular calls:

```text
GET /private/me/authorization
```

Response contains user display info, roles, effective permissions, and seller scopes necessary for UI rendering/route guards. UI authorization is UX only; backend always rechecks.

### 7.5 Backend authorization

Use `@PreAuthorize` with a Spring authorization bean. Long expressions use Java text blocks.

Example:

```java
@PreAuthorize("""
    @authorizationService.hasSellerPermission(
        'PRODUCT_UPDATE',
        #request.sellerId()
    )
    """)
```

### 7.6 UserContext

Controllers do not use `@AuthenticationPrincipal`.

A request-scoped `UserContext` provides current user identity/audit context. Kafka and scheduler executions create explicit system execution contexts with actor type such as SERVICE, SCHEDULER, or KAFKA_CONSUMER.

System/internal execution must never impersonate a human admin in audit logs.

### 7.7 Authorization cache and invalidation

Effective permission resolution may use L1 Caffeine + Redis. Permission assignment changes publish an authorization-changed event to invalidate caches promptly.

## 8. HTTP cross-cutting foundation

### 8.1 Trace context

Every HTTP request participates in OpenTelemetry/W3C Trace Context. Logs include traceId/spanId.

### 8.2 Request/response logging

Centralized structured logging records method/path/status/duration and safe payload details subject to configuration.

Mandatory masking includes authentication tokens, cookies, passwords, secrets, signatures, payment credentials, and other sensitive values. Payload logging has truncation/size limits.

### 8.3 Global trim

Centralized JSON/binding normalization trims String inputs. A field-level opt-out annotation/mechanism exists for values where whitespace is meaningful, such as password/signature/raw content.

Business code must not duplicate trim logic.

## 9. Exception model and i18n

Exception handling is a platform subsystem, not ad-hoc controller code.

Categories include:

- domain/business-rule exception;
- not found/conflict;
- validation;
- authentication/authorization;
- infrastructure;
- external dependency;
- retryable vs non-retryable classifications where operationally relevant.

Stable error codes are language-neutral API contracts. MessageSource resolves localized messages based on request locale/Accept-Language.

Standard response shape includes:

```json
{
  "code": "INVENTORY_INSUFFICIENT",
  "message": "localized message",
  "traceId": "...",
  "timestamp": "...",
  "path": "/private/checkout",
  "fieldErrors": []
}
```

Angular displays localized backend messages appropriately and may use error codes for UX behavior.

## 10. Kafka/event architecture

### 10.1 Event use

Kafka is the distributed domain-event backbone. It is not used blindly for synchronous query/command interactions that need immediate results.

### 10.2 Typed SerDe

Producer and consumer application code works with typed objects. Central Kafka configuration handles object -> bytes -> object conversion, error handling, headers, and tracing. Business consumers do not manually deserialize `byte[]`.

### 10.3 Event envelope/headers

Standard fields/headers include:

- eventId;
- eventType;
- eventVersion;
- occurredAt;
- aggregate/business key;
- correlationId;
- causationId;
- trace context;
- actor/audit context as appropriate.

### 10.4 Delivery semantics

Assume at-least-once delivery. Consumers must be idempotent. Use Inbox/deduplication where a duplicate side effect would be harmful.

### 10.5 DB + event consistency

Transactional Outbox is used for state changes that must reliably emit events.

### 10.6 Retry/DLT

Transient consumer failures use controlled retry topics/backoff. Permanent/threshold-exceeded failures route to DLT with enough metadata for operations inspection/replay.

## 11. Scheduler architecture

Schedulers create trace/job-run context just like HTTP/Kafka work.

Where multiple replicas exist:

- single-run jobs use a distributed scheduling/lock strategy such as ShedLock when appropriate;
- batch jobs are idempotent or checkpointed so restart does not corrupt/duplicate business state;
- job-run state stores processed/failed count, cursor/checkpoint where necessary, timestamps, status, traceId.

If a scheduler calls a private internal API, it uses service credentials and system user context.

## 12. Cache architecture

Redis use is explicit by use case, with declared role:

- CACHE;
- STATE_STORE;
- LOCK;
- COUNTER;
- RATE_LIMITER.

Common cache patterns:

- Cache Aside;
- optional Caffeine L1 + Redis L2 for hot stable metadata;
- TTL + jitter;
- invalidation via domain events where appropriate;
- stampede mitigation for hot keys.

Failure policy differs by role. A catalog cache outage should typically degrade to DB rather than kill catalog; a state-store role needs explicit persistence/recovery semantics.

## 13. Resilience and recovery

Reliability is a first-class architecture area.

### 13.1 Dependency policy

For each external dependency/operation define:

- timeout;
- whether retry is safe;
- retry count/window;
- exponential backoff + jitter;
- circuit breaker threshold/window;
- bulkhead/resource isolation where needed;
- fallback/degraded behavior;
- ambiguous-outcome handling;
- operational recovery path.

### 13.2 No blind retry

Retry only when semantics are safe/idempotent or when idempotency keys make them safe.

### 13.3 Payment unknown outcome

If provider outcome is unknown after timeout, mark the attempt UNKNOWN/PENDING_RECONCILIATION and query/reconcile before blindly issuing another charge.

### 13.4 Persistent Saga recovery

Checkout orchestration persists Saga/process state. Pod restart must not lose workflow progress. Recovery scheduler/worker resumes or compensates stuck workflows.

### 13.5 Kafka failures

Consumer DB/dependency transient errors -> retry topic/backoff -> DLT after threshold. Operations center supports inspect/retry/replay/resolve.

### 13.6 Kafka outage during publish

Business transaction commits state + outbox. Outbox publisher retries after broker recovery.

### 13.7 Service/pod failure

Kubernetes readiness removes unhealthy instances; replicas continue serving where applicable. Applications support graceful shutdown, stop taking new work, and allow in-flight HTTP/Kafka processing to finish within configured grace periods.

### 13.8 Dependency isolation

Search/notification failures must not roll back unrelated order/payment state. Failure boundaries are explicit.

## 14. Realtime notification

Notification source of truth is MongoDB.

Flow:

```text
Domain Event -> Kafka -> notification-service -> MongoDB -> realtime delivery -> Angular
```

Offline users retain persisted notifications. Redis may maintain unread counters/session routing as an optimization.

Realtime test must prove:

- logged-in user receives notification without refresh;
- unread count changes;
- notification persists in MongoDB;
- mark-read updates state/counter;
- Kafka consumer system context can call authenticated internal service through client credentials;
- failure/retry behavior works when auth/internal dependency is temporarily unavailable.

## 15. Marketplace business depth

### 15.1 Storefront

- marketplace home with banners/campaign/category sections;
- flash sale/hot deals;
- official/featured stores;
- recommendations/top sellers/recently viewed where sensible;
- category browsing;
- autocomplete/search/filter/sort/paging;
- detailed product page with gallery, variants, dynamic attributes, shop, rating/reviews, comments/Q&A, price/promotion, stock;
- guest/authenticated cart;
- checkout;
- payment;
- order history/tracking;
- comments/reviews;
- notifications;
- return/refund.

### 15.2 Seller portal

- seller dashboard;
- product/variant/media/dynamic attribute management;
- pricing and eligible seller promotions;
- inventory;
- seller order processing;
- product Q&A replies;
- settlement visibility;
- notifications.

### 15.3 Admin portal

- dashboards;
- user/role/permission/seller-scope administration;
- seller moderation;
- categories/brands;
- platform promotions;
- order/payment/refund/return visibility;
- review/comment moderation;
- operations center for DLT/stuck Saga/reconciliation/job failures;
- service health/observability links.

## 16. Multi-seller ordering

Checkout can contain multiple sellers.

One checkout creates a parent order and seller-specific suborders. Fulfillment/shipment can split further by warehouse/carrier.

Commission/payment fees lead to settlement calculations per seller. Seller settlement is handled by the dedicated finance bounded context.

## 17. Polyglot persistence rationale

The project intentionally uses several relational engines so the developer can compare real behavior while preserving clean boundaries.

- PostgreSQL: dynamic/catalog, pricing/promotion, inventory, payment, workflow-heavy services and Postgres-specific labs.
- MySQL: seller/shop/cart persistence where ordinary relational CRUD/transaction behavior is sufficient.
- MariaDB: review service for MySQL-family comparison.
- SQL Server: order/fulfillment transaction and indexing/query labs.
- Oracle: settlement/finance and Oracle SQL/plan learning.
- MongoDB: comment/notification flexible documents.
- OpenSearch: search/discovery read model.
- Redis: caching/counters/rate limiting/transient state only with explicit role.

Business/application interfaces hide engine-specific SQL from callers.

## 18. Database performance lab strategy

Baseline schema starts without speculative performance indexes/partitions beyond integrity-essential keys/constraints and indexes strictly required for correctness/runtime viability.

Dedicated labs build realistic datasets and compare:

- sequential/scan behavior;
- composite index column order;
- partial indexes;
- covering/index-only behavior;
- JSONB GIN;
- BRIN;
- offset vs keyset pagination;
- execution plans across engines;
- locking/deadlock scenarios.

PostgreSQL partition automation lab uses `pg_partman`. Candidate high-volume event/ledger/history tables are evaluated based on retention/query patterns rather than partitioned blindly.

## 19. Seed data

Provide profiles such as:

- `small`: quick demo but still visually populated;
- `medium`: tens of thousands of SKUs and meaningful orders/reviews/events;
- `performance`: hundreds of thousands to millions of rows in selected tables for query/performance labs.

Seed data includes dozens of categories, many brands, hundreds of sellers/shops, thousands+ products, tens of thousands+ SKUs, warehouses, customers, promotions, historic orders, reviews/comments, inventory history, notifications, and realistic images/placeholders that are legally safe to distribute.

Full default Compose uses the dataset required for a convincing demo; performance-scale seeding is opt-in if startup time would otherwise become unreasonable, but machine resource capacity is not treated as a limiting design assumption.

## 20. Docker Compose local platform

Canonical full stack includes:

- storefront Angular;
- admin/seller Angular application(s) as designed;
- API Gateway;
- Keycloak;
- all backend microservices;
- Kafka;
- Redis;
- PostgreSQL;
- MySQL;
- MariaDB;
- SQL Server;
- Oracle;
- MongoDB;
- OpenSearch;
- MinIO;
- OpenTelemetry Collector;
- Prometheus;
- Grafana;
- Loki;
- Tempo/trace backend;
- Kafka UI;
- CloudBeaver;
- Mailpit/Mailhog-like local mail sink;
- mock payment/shipping providers where external sandbox dependency would prevent reproducible local startup.

Compose uses health checks and dependency readiness so services do not simply race database startup.

## 21. WebFlux boundaries

Use WebFlux selectively:

- API Gateway: yes.
- high-concurrency non-blocking aggregation/external I/O services: where dependency chain is truly non-blocking and measurable benefit exists.
- JPA-heavy transactional services: default Spring MVC/JPA/JDBC; do not wrap blocking persistence in WebFlux and call it reactive.

## 22. Observability

### 22.1 Tracing

OpenTelemetry/W3C propagation across:

- Angular -> gateway -> HTTP services;
- HTTP client calls;
- Kafka producer/consumer;
- schedulers/jobs;
- system/internal calls.

### 22.2 Logging

Structured logs go to Loki-compatible pipeline. Log events include useful correlation fields and do not expose sensitive payloads.

### 22.3 Metrics

Expose infrastructure and business metrics such as:

- request rates/latency/errors;
- JVM/DB pool;
- Kafka lag/retry/DLT;
- outbox backlog;
- payment unknown/reconciliation counts;
- inventory reservation conflicts/expiry;
- checkout Saga stuck count;
- notification delivery/unread;
- cache hit/miss;
- scheduler duration/failure.

Grafana dashboards link metrics/logs/traces where practical.

## 23. Kubernetes generic deployment

Deliver Docker images plus Helm/generic Kubernetes resources for:

- Deployment/Service;
- ingress/Gateway API as chosen;
- ConfigMap/Secret;
- startup/readiness/liveness probes;
- resource requests/limits;
- HPA;
- PDB;
- service account;
- NetworkPolicy;
- rolling update/graceful shutdown.

No cloud-specific managed-service dependency is required by baseline deployment.

## 24. Testing strategy

### 24.1 Backend

Mandatory:

- unit tests;
- integration tests;
- architecture tests (ArchUnit);
- contract tests where cross-service contract risk justifies them;
- Testcontainers with real engines where practical;
- concurrency tests for inventory/reservation;
- idempotency/duplicate tests for payment/Kafka;
- outbox recovery tests;
- Saga recovery/compensation tests;
- resilience/circuit-breaker tests;
- scheduler recovery/idempotency tests.

Do not use H2 as a substitute for DB-specific behavior under test.

### 24.2 Frontend

Frontend unit tests are not required by project decision.

Required evidence instead:

- Angular production build succeeds;
- real browser smoke/E2E flows run against the composed system;
- screenshots captured for key anonymous/customer/seller/admin paths;
- screenshots include realtime notification and permission/error/i18n states.

### 24.3 Performance/resilience

Use k6 or equivalent for flash-sale/concurrency/load scenarios. Failure injection may use mock-provider controls, dependency shutdown, latency injection, or container/network tools that remain reproducible locally.

## 25. UI evidence set

At minimum final evidence contains actual running screenshots of:

- populated home;
- category/search results;
- product detail with variants/attributes/reviews/comments;
- cart;
- checkout;
- payment result;
- order tracking/history;
- realtime notification before/after;
- customer return/refund;
- seller dashboard/product/inventory/orders/settlement;
- admin dashboard/security permissions/seller scope;
- operations DLT/reconciliation/stuck workflow;
- localized validation/business error state.

## 26. Operational recovery center

Operations UI/API exposes safe tools for:

- inspect failed Kafka/DLT event;
- retry/replay where allowed;
- mark manually resolved with audit reason;
- inspect outbox backlog;
- inspect/recover stuck Saga;
- inspect/reconcile UNKNOWN payments;
- inspect failed scheduler runs/checkpoints;
- inspect consumer lag and service health references.

Manual recovery actions are permission-protected, audited, and idempotent where necessary.

## 27. Documentation deliverables

- Architecture design and ADRs.
- Per-feature specs under `.agent/specs/`.
- Implementation plans under `.agent/plans/` once specs are approved.
- `.agent/reports/` verification evidence.
- Markdown + DOCX authorization handbook.
- Markdown + DOCX failure/recovery handbook.
- Database performance lab docs with real queries/plans/results.
- Local runbook and troubleshooting guide.

## 28. Definition of done for final ZIP

The final ZIP is accepted only when:

1. Repository history/branch model is represented locally and code is organized according to specs.
2. `docker compose up -d --build` starts the canonical demo stack from documented prerequisites.
3. Angular storefront/admin/seller experiences are visually populated and usable.
4. Backend mandatory tests pass with fresh evidence.
5. Critical failure/recovery tests have evidence.
6. Kafka/database/observability browser UIs are accessible as documented.
7. Keycloak login and permission/seller-scope behavior are demonstrated.
8. Realtime notification is demonstrated with screenshots.
9. Full UI screenshot evidence is included.
10. Docs explain design decisions/trade-offs, not only setup steps.
11. Known limitations are listed explicitly rather than hidden.

## 29. Initial implementation order rationale

Platform/security/observability precede business services because later services must inherit consistent trace/log/error/auth/Kafka/test conventions. Catalog/seller establish marketplace identity; pricing/promotion/inventory/cart create checkout prerequisites; order/payment/settlement/fulfillment form transaction flow; notification/comment/review/return/operations then complete lifecycle and operational realism.

## 30. Review gate

No business implementation starts until this architecture design and the governance documents are reviewed. After approval, the next artifact is a detailed implementation plan for `feature/platform-foundation`, followed by test-first implementation and verification.

## 31. Frontend/Compose/DBA depth hardening (v11)

The platform demo is not considered complete when backend contexts exist without usable customer/operator workflows. Storefront/admin must consume the shared API envelope, page/cursor metadata, enum JSON codes and trace IDs through shared transport/facade layers. Browser code must never call `/internal/**`; operator actions that exist only internally are tracked as backend API gaps.

Canonical Docker Compose keeps one root `docker-compose.yml`, but backend and frontend application images use scoped build contexts (`./backend`, `./frontend`). Split compose fragments are root-relative and run with `--project-directory .`; parent build contexts are rejected. A wrapper pins the root compose to avoid accidental `COMPOSE_FILE` state.

Database performance material must cover PostgreSQL, MySQL, SQL Server and Oracle at comparable depth. The highest-level labs are incident packs with realistic skew/volume, actual plan evidence, engine-native wait/IO/memory/locking diagnostics, competing fixes, read/write/storage trade-offs, regression matrix, acceptance criteria and production decision. Keyword-only or single-query examples do not satisfy the deep-lab requirement.
