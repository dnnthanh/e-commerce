# Feature Spec 013 — Project-wide Deep Refactor and Use-case Enrichment

## Status

Java 25 is now the single repository baseline: Maven compiles with release 25, Maven Enforcer rejects non-Java-25 runtimes, `.java-version` is `25`, and backend/JMX Java container images use Temurin 25.

The **core production-depth flows** are implemented and source-verified across the major bounded contexts (Saga/idempotency/concurrency/recovery/read-model boundaries rather than CRUD-only wrappers). A second audit also found that the full Production-depth amendment contains **secondary/advanced use cases that are still not implemented end-to-end**. Those are explicitly recorded in `usecase/feature-013-gap-register/README.md`; Feature 013 must not be marked fully complete while that register or the Java-25 runtime acceptance gate remains open.

Production-relevant cases discovered during the refactor are now recorded under `usecase/`, and final audit/test evidence is indexed under `.agent/reports/013-codebase-deep-refactor/final-audit/`. Historical checkpoint logs remain immutable evidence of earlier runs.

The runtime acceptance gate is still environment-blocked here: this execution host exposes Java 21, cannot resolve `repo.maven.apache.org` for the Maven Wrapper distribution, and has no Docker daemon. Therefore Java-25 `mvn clean verify`, Testcontainers, Docker Compose startup and HTTP smoke tests are **not** claimed as passed in this environment.

## Problem

The repository has broad bounded-context coverage but many implementations are too thin for a
production-oriented learning project. Several modules collapse transport, application, domain,
persistence, SQL, mapping, validation, and error handling into very small classes. There are also
inconsistent naming conventions, compressed/unformatted Java sources, hard-coded business strings,
manual constructors/boilerplate, manual String normalization in business code, direct JDBC usage for
basic persistence cases, generic exceptions, and insufficient verification evidence.

The refactor must improve depth without adding empty architectural ceremony.

## Goals

- Runtime baseline is **Java 25 + Spring Boot 4.1.0 + Spring Cloud 2025.1.2**. Resilience4j uses framework-independent circuit-breaker/bulkhead core modules with explicit configuration; do not depend on the `resilience4j-spring-boot3` starter on the Boot 4 baseline.
1. Make service boundaries and object roles obvious from names and packages.
2. Introduce project stereotypes `@UseCase`, `@Persistence`, and `@Adapter`, all meta-annotated with `@Component`. `@UseCase` marks application input-port implementations, `@Persistence` marks persistence adapters, and `@Adapter` marks non-persistence infrastructure boundary adapters, inbound or outbound.
3. Prefer Lombok and existing libraries when they remove boilerplate without hiding semantics.
4. Prefer Spring Data JPA for basic relational persistence; retain JDBC/JdbcClient only for complex,
   native, reporting, bulk, locking, or performance-sensitive queries with an explicit reason.
5. Keep MongoDB bounded contexts on MongoDB.
6. Replace magic business strings/numbers with enums or named constants where appropriate.
7. Split exceptions by concern and remove generic `IllegalArgumentException`/`RuntimeException`
   from business flows.
8. Expand thin bounded contexts with realistic use cases, invariants, failure modes, idempotency,
   concurrency, outbox/inbox, recovery, pagination, authorization and observability where the master
   plan requires them.
9. Apply formatter/static-quality rules and remove avoidable Sonar-style warnings.
10. Produce build, test, startup and smoke-test evidence before claiming completion.

## Non-goals

- Do not create interfaces, wrappers, layers, or value objects with no behavioral/architectural value.
- Do not replace complex SQL with JPA merely to avoid JDBC.
- Do not migrate MongoDB bounded contexts to relational persistence.
- Do not add performance indexes/partitions outside the dedicated database labs.
- Do not rewrite every service simultaneously without verification checkpoints.

## Architectural stereotypes

Create technical annotations in the shared platform starter:

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface UseCase {
}
```

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Persistence {
}
```

Rules:

- `@UseCase`: application-layer command/query orchestration and transaction boundary.
- `@Persistence`: outbound persistence adapter implementation.
- `@Adapter`: non-persistence infrastructure boundary adapter, inbound or outbound, such as internal/external REST/gRPC, webhook/messaging consumer, scheduler/worker, provider, cache, storage, rate-limit, search, or outbox adapter.
- True domain services remain plain domain objects unless Spring wiring is genuinely required.
- Scheduled jobs and Kafka consumers use `@Adapter` in addition to their scheduling/listener annotations when they represent infrastructure boundaries. Controllers retain Spring MVC annotations; configuration classes retain configuration/property annotations.
- Repository interfaces backed directly by Spring Data keep Spring Data semantics; the adapter that implements an application/domain port uses `@Persistence`.

## Package convention

Preferred service layout:

```text
<bounded-context>/
  api/
    request/
    response/
  domain/
    model/
    enumtype/
    constant/
    exception/
    service/
  application/
    command/
    query/
    dto/
    exception/
    port/
      in/                     # XUseCase / XQuery interfaces only
      out/                    # XPersistencePort / XClientPort interfaces only
    service/                  # XServiceImplement and internal application collaborators
  adapter/
    in/
      web/
      internal/
        rest/
        grpc/
      webhook/
      messaging/
        kafka/
      scheduler/
    out/
      persistence/
        entity/
        repository/
        projection/
        mapper/
      internal/
        <target-service>/
          rest/
          grpc/
      external/
        <provider>/
          <protocol>/
      messaging/
        kafka/
  configuration/
```

Packages may be collapsed for genuinely small contexts, but exception types, transport objects,
persistence entities and domain models must not be mixed in a single catch-all package.

## Naming convention

Use suffixes that communicate role:

- Relational persistence: `*JpaEntity`, `*JpaRepository`, `*PersistenceAdapter`.
- Mongo persistence: `*Document`, `*MongoRepository`, `*MongoPersistenceAdapter`.
- Transport: `*Request`, `*SearchRequest`, `*Response`.
- Application: `*Command`, `*Query`, `*Criteria`, `*Dto`.
- Read-only persistence: `*Projection`.
- Domain: business noun without persistence suffix where practical.
- Mapping: `*ApiMapper`, `*PersistenceMapper`, `*EventMapper`.
- Events: `*Event`.
- Exceptions: meaningful names such as `OrderNotFoundException`, `InvalidOrderTransitionException`, `DuplicateCommentException`.

Avoid ambiguous names such as `Data`, `Info`, `Model`, or generic `Service` when a more precise role exists.

## Lombok and library policy

- Use `@RequiredArgsConstructor` for Spring components with required final collaborators.
- Use `@Getter`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor` selectively.
- Do not use Lombok `@Data` blindly on JPA entities or rich domain models.
- A stateless constants/helper holder must use Lombok `@UtilityClass`; do not alternate between a
  hand-written `final class + private constructor` and `@UtilityClass` for the same role.
- Use Bean Validation for request constraints.
- Use centralized trimming configured by the platform; business use cases must not call `.trim()`.
- Use Apache Commons Lang/Collections when it removes repeated, non-business boilerplate; do not add
  a dependency for a single trivial JDK operation.
- MapStruct is mandatory for non-trivial mapping between API/application/domain/persistence models.
  Business decisions remain in domain/use-case code; exceptional identity/version synchronization
  that MapStruct would obscure may stay explicit and must be documented.

## Application boundary / SOLID policy

- Every application use case invoked from a controller, scheduler, Kafka consumer/listener or another bounded
  context must implement an **input-port interface** (`*UseCase`, `*Command`, `*Query` according to
  responsibility).
- Inbound adapters depend on the input-port interface, never on the concrete `@UseCase` implementation.
- `application/**` and `adapter/out/**` must not import HTTP transport request/response classes. Boundary mapping is done with MapStruct.
- Internal REST adapters use typed request/response DTOs; raw `Map.class` business responses and non-trivial `Map.of(...)` wire payloads are forbidden.
- Boot 4 modules injecting `RestClient.Builder` declare `spring-boot-starter-restclient`. Background deployables do not carry MVC/WebFlux server starters unless they expose an HTTP endpoint.
- Application input ports live under `application/port/in` (or an equivalent clearly named package).
- Output ports remain interfaces owned by application/domain and adapters implement them.
- Do not create an interface only for a private helper with no architectural boundary; the rule
  exists to enforce Dependency Inversion at inbound boundaries, not interface-count padding.
- Split command/query interfaces when they have materially different authorization, transaction or
  read/write behavior; a cohesive interface is acceptable for a small bounded application surface.

## Enum and constant policy

Business states and closed sets use enums, for example:

- order status / transition / cancellation reason;
- comment status / moderation status / reaction type;
- payment state / provider / outcome;
- fulfillment status;
- notification type/channel.

Technical constants use Lombok `@UtilityClass` or typed configuration properties. Topic names, event
names, cache names, limits and retry-related values must not be duplicated as string literals across
business code.

## Persistence policy

The persistence choice is deterministic and must not drift between use cases:

### Spring Data JPA — simple write/read persistence

**Simple CRUD must use Spring Data JPA** for relational aggregates:

- `save`, `findById`, `existsBy...`, simple unique-key lookup;
- aggregate lifecycle persistence that maps naturally to JPA;
- optimistic locking through `@Version`;
- simple relationship loading using entity graphs/projections when appropriate.

Do not use `JdbcClient` merely because writing SQL is quick.

### Native SQL — relational search/filter/pagination

**Relational search/filter/pagination must use native SQL, even when the predicates look simple.** Do not implement business search with
JPA `Specification`, Criteria API or a long chain of derived repository methods.

Preferred order:

1. Spring Data repository `@Query(nativeQuery = true)` / `@NativeQuery` for stable search SQL;
2. JPA `EntityManager#createNativeQuery` for dynamic native SQL where repository annotations become
   unreadable;
3. JDBC only when the search/query itself is sufficiently complex or performance-sensitive to
   justify explicit row mapping and database control.

Native search SQL must define deterministic ordering and pagination, use projection DTOs where the
full aggregate is unnecessary, and have database-lab coverage for important indexes/query plans.

The dedicated Search/Discovery bounded context may use OpenSearch for full-text/facet discovery; the
native-SQL rule applies to relational service search endpoints and relational fallback/read models.

### API/search contract shape

- `*Api` interfaces contain endpoint annotations/signatures only. Request/response DTOs are top-level types in `api/request/**` and `api/response/**`; nested transport records are prohibited.
- Search/filter/list APIs use one grouped `*SearchRequest` or `*QueryRequest` rather than multiple loose filter `@RequestParam` arguments.
- The web adapter maps transport requests to application `*SearchCriteria` / `*QueryCriteria` via MapStruct. Application ports must not import web DTOs.
- Relational search repositories accept one grouped criteria parameter and bind fields with Spring SpEL in native SQL, e.g. `:#{#criteria.status}`. Stable pagination ordering is mandatory.
- This does not force command parameters, resource path identifiers, atomic mutations, locks, or provider callbacks into artificial search objects.
- Search/Discovery continues to use OpenSearch; only its HTTP request is grouped. The native-SQL requirement applies to relational search/read-model endpoints.

### JDBC/JdbcClient — complex/optimized database work only

**JDBC is reserved for complex** database-specific operations such as:

- recursive or CTE-heavy queries;
- atomic conditional updates / explicit locking (`SKIP LOCKED`, vendor lock hints, CAS-style stock);
- native bulk write/merge/upsert pipelines;
- high-volume ledger/reporting/read models where measured performance justifies explicit mapping;
- transactional outbox/inbox SQL that requires vendor JSON/locking features;
- performance labs and queries where JDBC is measurably clearer/faster than JPA/native repository APIs.

Every retained JDBC adapter must have a class-level rationale saying why JPA/native repository APIs
are insufficient. Ordinary CRUD through JDBC is a refactor failure. A mixed JPA + JDBC adapter is allowed only
when the JDBC portion is a clearly isolated complex/vendor-specific concern (for example `SKIP LOCKED`, atomic
conditional mutation, or vendor JSON/locking SQL); the aggregate CRUD itself stays JPA.

## Exception policy

Exceptions are separated by concern:

```text
domain/exception/
application/exception/
exception/infrastructure/
adapter/in/web/error/
```

- Domain exceptions represent invariant/business-rule failure.
- Application exceptions represent use-case coordination failure.
- Infrastructure exceptions represent DB/broker/external dependency failure.
- HTTP translation remains centralized and emits stable error codes + i18n message.
- No raw stack traces are returned.
- Avoid generic `IllegalArgumentException` for business cases.

## Formatting and static quality

- Add a deterministic Java formatter to the Maven quality gate.
- Format all modified Java files.
- Eliminate avoidable Sonar-style findings: duplicated literals, generic exceptions, needless complexity,
  unused imports/code, resource leaks, swallowed exceptions, raw types, field injection, magic numbers,
  mutable public state, overlong parameter lists.
- Preserve JavaDoc only where it adds contract semantics, units, ownership, invariants, constraints, or error behavior; private fields and obvious implementation details do not require JavaDoc. Remove comments that merely restate names.
- Remove stale parameter docs after DTO extraction and generated comments such as “HTTP adapter”, “creates client”, or comments that only restate a field/class name.
- `spring-boot-starter-test` is test-scoped; inherited Lombok is not duplicated in child modules; unused imports/dependencies fail the source-hygiene gate.
- Java 25 is the single backend/runtime baseline. `AGENTS.MD`, `.agent/CONVENTIONS.MD`, Maven compiler
  release, backend/container Java images and `.java-version` must stay aligned. Maven Enforcer fails fast
  when the reactor is launched with a JDK outside Java 25.

## Refactor waves

### Wave 1 — Platform and quality foundation

- `@UseCase` and `@Persistence`.
- formatter/static-quality plugins.
- shared error hierarchy/contracts.
- shared constants/config conventions.
- test utilities.
- architecture tests for layer boundaries and stereotypes.

### Wave 2 — High-value business contexts

Refactor and deepen:

1. order;
2. checkout;
3. inventory;
4. payment;
5. fulfillment;
6. return.

These contexts must demonstrate non-trivial state transitions, idempotency, concurrency,
transaction boundaries, recovery, outbox/inbox and query/pagination patterns as appropriate.

### Wave 3 — Interaction contexts

Refactor and deepen:

1. comment;
2. review;
3. notification;
4. seller;
5. cart.

Comment must include realistic thread/reply/moderation/soft-delete/reaction/report/search flows while remaining MongoDB-owned.

### Wave 4 — Remaining contexts

Catalog, pricing, promotion, search, media, audit, authorization, settlement, operations and associated
workers/outbox deployables receive the same naming, stereotype, exception, formatting, library and verification
treatment without inventing unnecessary business complexity.

## Required depth: order bounded context

`be-order-*` must no longer be a thin CRUD sample. Minimum capabilities:

- parent marketplace order + seller orders + lines;
- explicit order status state machine;
- create from confirmed checkout command;
- idempotent creation by checkout/request key;
- seller/customer/admin query models;
- pageable order search with criteria object;
- cancel request and cancellation policy;
- partial seller-order cancellation where valid;
- payment status handling through idempotent inbox consumer;
- fulfillment/shipment status handling;
- optimistic concurrency/versioning where concurrent transitions are possible;
- outbox events for meaningful state changes;
- audit metadata;
- failure/recovery tests;
- JPA for basic persistence and targeted native/JDBC only when justified.

## Required depth: comment bounded context

Minimum capabilities:

- create thread;
- reply to comment/thread;
- edit own comment under policy;
- soft delete while preserving descendants;
- moderation hide/unhide;
- report;
- reaction add/remove with idempotency;
- thread/reply pagination;
- mention extraction delegated to a dedicated component;
- anti-spam/rate-limit port;
- authorization policy;
- durable Mongo source of truth;
- transactional Mongo outbox where supported by current architecture;
- duplicate event/request handling;
- meaningful domain exceptions and enums.

## Testing strategy

- Domain unit tests for invariants/state machines/policies.
- Application/use-case tests for orchestration and port interaction.
- Repository tests for JPA/Mongo mappings and query semantics.
- Controller contract tests for validation/error shape.
- Integration tests with Testcontainers where infrastructure behavior matters.
- Concurrency/idempotency tests for inventory/order/payment/comment reactions where applicable.
- ArchUnit tests for controller -> use case -> domain/port boundaries.
- Regression tests for every reproduced build defect.

## Verification gate

A refactor wave is complete only when evidence contains:

1. clean compile;
2. unit tests;
3. applicable integration tests;
4. architecture/static-quality checks;
5. formatter check;
6. deployable packaging;
7. local startup for affected deployables;
8. smoke tests of representative APIs;
9. Docker Compose health where infrastructure is required;
10. report saved under `.agent/reports/013-codebase-deep-refactor/<wave>/`.

Example evidence must include command, exit code, relevant summary and timestamp.

## Initial audit findings

The uploaded repository currently shows:

- dozens of `@Service` classes that represent application use cases;
- many manual constructors despite Lombok already being present in the parent POM;
- several compressed single-line Java files that are not maintainably formatted;
- direct `JdbcClient` usage in multiple basic application/persistence flows;
- manual `.trim()`/blank checks inside domain/application code despite the platform trim rule;
- hard-coded statuses/event types/topic names in several services;
- generic `IllegalArgumentException` in business flows;
- `be-order-api` contains only a small set of Java classes for a context expected to model a marketplace order lifecycle;
- `be-comment-api` is similarly thin relative to the Phase 11 requirements;
- the uploaded archive does not contain `.git`, so branch creation/commit evidence cannot be produced from this snapshot;
- The current execution environment exposes Java 21, while the repository is intentionally pinned to Java 25. A Maven Wrapper is now present and the reactor has a Java-25 Enforcer rule, but this environment cannot download the Maven distribution because external DNS resolution is blocked. Final Java-25 reactor evidence therefore remains pending until execution in a Java-25/network-capable environment.

## Acceptance criteria

- New stereotypes are used consistently.
- Modified Spring components use constructor injection and Lombok where appropriate.
- No business use case performs manual input trimming.
- Basic persistence does not use JDBC without justification.
- Business closed sets are typed as enums rather than repeated string literals.
- Exceptions are discoverable by concern/package.
- Naming makes transport/domain/persistence/read-model roles clear.
- High-value contexts contain realistic use-case depth rather than single-method demonstrations.
- Modified code is formatted and passes the agreed static-quality gate.
- Build/test/start/smoke evidence exists and is reproducible.

## Production-depth amendment — mandatory, not optional

Feature 013 MUST NOT be considered complete when a bounded context only exposes CRUD or a thin `repository.save(...)` use case. Every major bounded context must contain realistic invariants, failure paths, idempotency/concurrency decisions, event/recovery behavior, query models, authorization, observability, and applicable integration tests.

### Mandatory depth by bounded context

- **Catalog / dynamic attributes:** SPU/SKU/variant model; category tree with cycle-safe re-parenting; typed dynamic attributes and category applicability; variant combination generation and duplicate prevention; draft/review/active/suspended/archive lifecycle; seller listing overlay; content versioning/audit; bulk import with row-level result and resumability; product-detail projections; search/cache invalidation events; concurrent edit protection. Demonstrate Strategy for typed validation, Factory for typed values, native-SQL search, projections/entity graphs, outbox.
- **Media:** upload initiation; metadata validation; initiated/uploaded/scanning/processing/ready/failed state machine; malware-scanner port; deterministic image variants; checksum/idempotent processing; duplicate binary handling; retry without duplicate variants; attach/detach ownership policy; reference-safe delayed deletion; DLQ/replay; CDN/public URL abstraction.
- **Search / discovery:** incremental indexing and full reindex with checkpoint/resume; keyword/category/brand/seller/price/availability/dynamic facets; relevance/price/newest/popularity sort; autocomplete/typo strategy; facet aggregation; search-after/cursor pagination; zero-result analytics; versioned index alias swap; stale-event protection; degraded behavior when search engine is unavailable.
- **Pricing:** base/seller/channel/scheduled prices; effective-date and priority resolution; BigDecimal currency/rounding policy; history/audit; scheduled price cancellation; bulk updates with partial validation; immutable checkout quote; optimistic concurrency; cache invalidation/events; deterministic overlap resolution. Demonstrate Strategy/chain for price resolution.
- **Promotion:** coupon and automatic campaigns; eligibility by time/seller/category/SKU/customer segment/minimum spend/channel; fixed/percentage/tiered/shipping benefits; stacking/exclusion/priority; global/customer/order limits; reserve-confirm-release usage; race-safe redemption; explain/simulation result; deterministic discount allocation to seller orders/lines; lifecycle; idempotent redemption/rollback; manual override audit. Demonstrate domain Specification/Composite for in-memory eligibility rules, Strategy, Chain, and reservation pattern; relational search remains native SQL.
- **Inventory:** SKU+warehouse balances with on-hand/available/reserved/damaged/in-transit; atomic/idempotent reservation; confirm/consume/release; TTL expiry; partial reservation policy; transfer; adjustment with reason; inventory ledger; oversell prevention; duplicate/out-of-order event handling; reconciliation; low-stock event; bulk sync job/item status and retry. Include contention tests comparing atomic update/locking approaches.
- **Cart:** guest/authenticated carts; login merge conflict policy; add/update/remove; optimistic cart version; selected lines; validation against product/price/inventory/purchase limits; coupon preview; stale-price indication; expiration; save-for-later; idempotent mutations; quantity limits; multi-seller grouping; explicit Redis durability/cache semantics.
- **Checkout:** stateful orchestration with idempotency key; cart snapshot validation; address/seller/listing validation; immutable pricing quote; promotion reservation; inventory reservation; shipping quote; checkout snapshot; payment initiation; exactly-once order creation; confirmation/compensation; resumable state; recovery worker. Must test timeouts after reservation, partial dependency failure, lost HTTP response, duplicate submit/callback, stuck workflow. Demonstrate Saga/process manager + state machine + outbox/inbox; never hold DB transaction across remote HTTP.
- **Order:** marketplace parent order + seller orders + immutable lines/address/payment snapshots; explicit state machines; exactly-once creation from checkout; customer/seller/ops/admin projections; native-SQL pageable search; customer/seller/admin cancellation policies; partial seller/line cancellation; idempotent payment and fulfillment event handling; unpaid expiry; timeline/history; guarded manual override; notes/tags; outbox/inbox; optimistic concurrency; reconciliation/read-model diagnostics.
- **Payment / refund:** multiple attempts without duplicate capture; provider Strategy/Adapter; redirect/QR/tokenized flow without storing prohibited card data; signed webhook verification; idempotent callbacks; payment state machine; authorization/capture; UNKNOWN result state; provider query/reconciliation worker; full/partial/cumulative refunds; refund idempotency; ledger/provider refs; out-of-order protection; DLQ/replay; provider settlement reconciliation; outbox.
- **Fulfillment / shipping:** quote abstraction; split by seller/warehouse; allocation; idempotent carrier request; pick/pack/ready-to-ship lifecycle; multiple packages; partial shipment; signed/idempotent carrier webhook; out-of-order tracking protection; delivery failure/retry/return-to-sender; pre-handoff cancellation; timeline; SLA breach detection; carrier reconciliation; outbox.
- **Return / refund orchestration:** eligibility by delivered time/category/seller/reason; partial quantities; evidence/media; approve/reject; return shipment; receive/inspect; accepted/partial/rejected outcomes; refund calculation from immutable order allocations; idempotent payment refund; restock/scrap/quarantine; cumulative quantity/amount protection; state machine; dispute/escalation; recovery when refund succeeds but downstream updates fail; audit timeline.
- **Review:** verified-purchase eligibility; uniqueness policy; rating/title/content/media; edit window/history; soft delete; seller response; helpful reaction idempotency; reporting; moderation queue; hide/unhide/reject/restore; event-driven rating projection; anti-spam/rate-limit; pluggable suspicious-review policy; pagination/sorting; moderation audit.
- **Comment:** MongoDB source of truth; root/reply; explicit depth strategy; edit-window/authorization policy; soft-delete parent preserving descendants; moderation; duplicate-report protection; idempotent reactions with concurrency-safe counters; independent thread/reply pagination and cursor path for hot threads; mention normalization/dedup + notification event; anti-spam/rate-limit; duplicate-content and blocked-user policy hooks; atomic/versioned Mongo edits; transactional Mongo outbox when topology supports it; inbox dedupe; moderator audit; tests for parent deletion with children, concurrent reactions, duplicate reports and retried create/reply.
- **Notification:** channel/event preferences; durable in-app inbox; email/push/SMS ports; localized/versioned templates; handler/strategy mapping instead of giant switches; dedupe; retry/backoff; DLQ/replay; read/unread/mark-all/cursor pagination; batch fan-out; quiet hours/opt-out; provider rate limiting; delivery attempts; WebSocket/SSE only as acceleration over durable inbox; trace propagation.
- **Seller:** onboarding lifecycle; profile/store config; verification/approval/suspension; staff/member roles; warehouses/pickup addresses; listing association; operational settings; dashboard projections; seller data isolation; privileged audit; suspension/config events.
- **Authorization/security:** role + resource/ownership policies; customer/seller/ops/admin boundaries; no scattered role strings; seller/tenant isolation tests; invalidation design aligned with auth; sensitive-operation re-auth hook; abuse rate limiting; security audit; secrets/sensitive data redaction; forbidden cross-owner contract tests.
- **Audit:** append-only business audit; actor/action/resource/before-after summary/correlation/time; separate from debug logs; pageable search; redaction; idempotent ingestion; retention/partition lab; tamper-evidence option for critical records.
- **Settlement:** immutable seller payable ledger from captures/discounts/commission/shipping/refunds/adjustments; period close; statements; later-period refund adjustments; audited manual adjustment; provider reconciliation; idempotent event consumption; balance rebuild; payout-ready/reference abstraction; dispute/hold; large-ledger pagination/partition lab.
- **Operations:** diagnose stuck checkout/order/payment/fulfillment/outbox/inbox; guarded idempotent retry/replay; DLQ inspection; reconciliation commands; audited state override with reason; kill switches; health/readiness/dependency diagnostics; bulk job with per-item failures; no direct controller-to-DB row-fix endpoints.

### Mandatory cross-context scenarios

The integration suite must prove: purchase happy path (Cart -> Checkout -> Pricing -> Promotion -> Inventory -> Payment -> Order -> Fulfillment -> Notification -> Settlement); checkout compensation with injected failures at multiple boundaries; concurrent payment polling/webhook race; last-unit oversell race; partial fulfillment followed by partial return/refund/settlement adjustment; comment/review concurrent reaction + moderation + duplicate event; and outbox crash-after-send-before-mark recovery with downstream inbox dedupe.

### Mandatory pattern coverage

Patterns are required only where they solve the concrete problem: Strategy (payment/promotion/shipping/attribute validation), Factory (runtime provider/typed value construction), native SQL for relational search and Specification/Composite only for in-memory/domain eligibility, State (checkout/order/payment/fulfillment/return), Chain of Responsibility (promotion or moderation pipeline), Ports/Adapters (external systems), Saga/Process Manager (checkout and return/refund), Repository, transactional Outbox, Inbox/Idempotent Consumer, optimistic locking, contention-sensitive atomic/pessimistic DB operation with evidence, CQRS-lite/read projections, cache-aside, circuit breaker/bulkhead, and event-driven Observer via Kafka. Pattern usage must be documented with the business reason; no pattern-count padding.

### Required libraries

Use established libraries when they improve correctness/readability: Lombok, MapStruct, Bean Validation, Spring Data JPA for CRUD plus native SQL for relational search, Spring Data MongoDB, Apache Commons Lang/Collections where genuinely clearer, Resilience4j, Caffeine, Testcontainers, Awaitility, ArchUnit, WireMock/MockWebServer, Micrometer and OpenTelemetry. Library usage is not itself a goal; do not add dependencies for trivial JDK operations.

### Required test matrix

Each context must include the applicable combination of domain invariant tests, use-case orchestration tests, repository integration tests, API validation/error-contract tests, idempotency tests, concurrency tests, outbox/inbox tests, failure injection, recovery/reconciliation tests, security/ownership tests, Awaitility-based async tests, and ArchUnit boundary tests. Tests must assert business outcomes, not only Mockito invocation counts.

### Performance/resilience labs

Required evidence labs include JPA entity vs projection vs JDBC/native; offset vs keyset pagination; N+1 reproduction/fix; optimistic vs pessimistic locking; atomic inventory update vs read-modify-write; Redis cache hit/miss/stampede; Caffeine vs Redis; Kafka consumer/partition/lag; outbox polling with `SKIP LOCKED`; batch write tuning; PostgreSQL partial/covering/functional/GIN/BRIN indexes; `EXPLAIN (ANALYZE, BUFFERS)` before/after; `pg_partman`; Hikari pool exhaustion caused by remote I/O inside transaction followed by corrected design; retry/circuit-breaker/bulkhead failure injection; and reproducible load tests. Every lab documents hypothesis, setup, command, measured result, interpretation and production trade-off.

### Definition of Done — explicitly rejects basic implementations

A context is NOT done if it only has CRUD; its main use case is essentially `repository.save(...)`; business states remain repeated strings; controller reaches repository directly; ordinary CRUD uses JDBC without rationale; generic business exceptions remain; relevant concurrency/idempotency is untested; publish-after-commit events lack outbox; consumers are not duplicate-safe; required search loads all rows; remote I/O occurs inside DB transaction; naming mixes request/response/domain/persistence/projection roles; code is unformatted; or fresh build/test/start evidence is absent.

Feature 013 can be marked complete only after a Java 25 toolchain runs the full reactor, `mvn clean verify` and formatter/static/architecture checks exit 0, applicable Testcontainers suites pass, Docker Compose starts required infrastructure/deployables, representative APIs and mandatory cross-context scenarios pass, unexplained basic JDBC/manual trimming/magic business strings/generic business exceptions are removed, Sonar-style blocker/critical findings introduced by the feature are zero, and `.agent/reports/013-codebase-deep-refactor/final/` contains reproducible commands/timestamps/exit codes/results. Every major context must also contain a short document mapping use case -> invariant -> chosen pattern -> persistence/concurrency decision -> tests.


## Final source implementation evidence

The final service-depth pass adds and verifies the following repository-level gates:

- canonical service-depth coverage for Order, Checkout, Payment, Fulfillment, Return, Inventory,
  Cart, Promotion, Review, Comment, Notification, Settlement, Authorization and Operations;
- no application implementation class named as a generic `*Service` where a use-case name applies;
- no controller directly depends on `JdbcClient`, `JpaRepository` or `MongoTemplate`;
- no production service source directly instantiates generic `IllegalArgumentException`,
  `IllegalStateException` or `RuntimeException`;
- Catalog ordinary Product CRUD uses Spring Data JPA + `@Version`; relational product search/filter/pagination uses native SQL rather than JPA Specification/Criteria;
- Notification delivery uses atomic lease/claim semantics;
- Cart concurrent first-create is duplicate-safe;
- Return inventory-disposition events are transition-sensitive;
- Settlement reconciliation preserves periods with zero ledger entries;
- realistic seed distribution and advanced SQL/index/partition labs remain green.

The fresh final non-Maven regression executes every `verification/verify_*.py` and
`verification/verify_*.sh` script and records `OVERALL=0`. This is deliberately separated from the
blocked Java-25/Maven/Docker runtime gate described above.
## Architecture amendment — input/output naming, internal adapters and CI/CD mapping

This section is mandatory and overrides older examples that use bare `*Implement`, concrete `*UseCase`, flat `application/port`, or `adapter/out/remote` naming.

### Input/application naming

- `application/port/in/XUseCase.java` or `XQuery.java`: interface only.
- `application/service/XServiceImplement.java`: concrete `@UseCase` implementation.
- Inbound adapters depend only on the input-port interface.
- Concrete application collaborators that are not inbound boundaries use a role name such as `*Service`, `*Policy`, `*Coordinator`, or `*Resolver`; they must not be named `*UseCase`.

### Output naming

- All output contracts live in `application/port/out` and are interfaces ending `Port`.
- Concrete outbound implementations are adapters. Persistence examples use `*PersistenceAdapter`; internal REST examples use `*RestAdapter`; gRPC examples use `*GrpcAdapter`.

### Internal communication package and configuration

- Internal outbound: `adapter/out/internal/<target-service>/rest|grpc`.
- Internal inbound: `adapter/in/internal/rest|grpc`.
- Webhook inbound: `adapter/in/webhook`.
- Kafka inbound/outbound: `adapter/in/messaging/kafka`, `adapter/out/messaging/kafka`.
- Third party: `adapter/out/external/<provider>/<protocol>`.
- Do not add a runtime transport selector. Protocol is selected by the implemented adapter and deployment architecture.
- No internal service URL may be hard-coded in Java. `application.yml` exposes environment-backed endpoint properties without environment-specific defaults.

### Supported communication mechanisms

REST, gRPC, GraphQL, Kafka event, work queue, webhook, and SOAP are recognized mechanisms. The implementation must document why one is selected for a specific interaction; these are not interchangeable runtime modes.

### CI/CD configs

- `ci-cdconfigs` is an in-repository project.
- Every `be-*` deployable owns one direct folder under `ci-cdconfigs`; no global mapping file is allowed.
- Each folder contains non-secret environment mapping examples and Kubernetes base + dev/staging/prod overlays.
- Secret values are never committed or defaulted in application config; example files contain keys/placeholders only and runtime values come from CI/Secret Manager/Kubernetes Secret/External Secrets.

