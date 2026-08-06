# AGENTS.MD

## Purpose

This repository is a production-oriented, multi-seller marketplace. Agents must optimize for correctness, maintainability, operability, and learning value rather than speed or code volume.

## Branch policy

- `master` is production-ready only.
- `develop` is the integration/core branch.
- Every change starts from `develop` on `feature/*`, `fix/*`, or `chore/*`.
- No direct implementation work on `master` or `develop`.
- A feature may merge into `develop` only after its spec, implementation plan, tests, verification evidence, and review are complete.

### Automatic squash policy for AI-assisted branches

- **Default behavior:** when AI-assisted work on one branch represents one logical change, the agent MUST automatically squash AI-created implementation, fixup, CI-follow-up, retry, documentation-follow-up, and cleanup commits before the final handoff. Do not wait for the user to remind you to squash.
- Preserve intermediate commit history only when the user explicitly asks to keep it or when the branch intentionally contains multiple independently reviewable logical changes.
- For one logical feature/fix, the merge-ready branch MUST contain exactly one logical commit on top of its target base branch.
- Perform the squash only after all intended source, test, CI, and documentation changes are present so follow-up commits are included in the final logical commit.
- After any squash, rebase, history rewrite, or force-update, rerun all required CI checks against the exact rewritten `HEAD` SHA.
- Verify the final compare/PR metadata reports the intended commit count before declaring the branch merge-ready.
- Never report the branch as merge-ready until the required checks are green for that exact final SHA.

## Mandatory workflow

Before implementing any feature:

1. Read this file.
2. Read `.agent/PLAN.MD`.
3. Read `.agent/CONVENTIONS.MD`.
4. Read the relevant `.agent/specs/<feature>.md`.
5. If the feature spec does not exist or is materially incomplete, stop and write/review the spec first.
6. Create or switch to a feature branch from `develop`.
7. Write/update tests before or alongside implementation.
8. Implement the smallest coherent design that satisfies the approved spec.
9. Run the feature's full quality gate.
10. Store verification evidence under `.agent/reports/`.
11. Update docs/ADR/spec when implementation changes an architectural decision.
12. Only then prepare merge/release artifacts.

## Architecture rules

- Use DDD with rich domain models where behavior/invariants justify it.
- Follow Clean/Hexagonal boundaries: inbound adapter -> application/use case -> domain -> port -> outbound adapter.
- Domain and application layers must not depend on persistence entities or adapter classes.
- Controllers must not call repositories directly.
- A microservice must not access another microservice's database.
- Cross-service synchronous calls use defined HTTP clients; distributed domain events use Kafka.
- Use a work queue only when the problem is work-distribution semantics rather than domain-event streaming.
- Do not create a shared business-domain library that couples services. Shared libraries may contain stable technical conventions/contracts only.
- Every backend deployable/container/artifact starts with `be-`; Java package roots use `com.dnnthanh.marketplace.be.<context>...`.
- A bounded context may split `api`, `worker`, `job`, and `outbox` into independent deployables when workload/failure isolation justifies it; do not create a global outbox/job service that owns unrelated contexts.
- Inbox is normally a consumer-side persistence/idempotency pattern inside a worker, not automatically a standalone service.

## Port, implementation, and internal-integration naming

- Application input contracts are interfaces in `application/port/in` and end with `UseCase` or `Query`, for example `CartUseCase`.
- A class implementing an input-port interface lives in `application/service` and ends with `ServiceImplement`, for example `CartServiceImplement`. Do not use `*UseCaseImpl`, bare `*Implement`, or a concrete class named `*UseCase`.
- Output contracts are interfaces in `application/port/out` and end with `Port`. Persistence examples: `CartPersistencePort`; internal dependency examples: `PricingClientPort`, `InventoryAvailabilityPort`.
- Non-persistence infrastructure boundary adapters, inbound or outbound, end with an architectural role name and use the project `@Adapter` stereotype. Examples include internal/external REST or gRPC adapters, webhook/messaging consumers, schedulers/workers, provider/cache/storage/search adapters, and outbox publishers. Persistence adapters use `@Persistence`; application input-port implementations use `@UseCase`. Do not use generic `@Component` when one of these architectural stereotypes applies.
- HTTP transport request/response types stay under `api.request` / `api.response` and MUST NOT be imported by `application/**` or `adapter/out/**`; MapStruct maps transport models to application command/query/result models.
- Internal REST contracts use typed request/response DTOs. Do not parse business responses with raw `Map.class` or construct non-trivial transport payloads with `Map.of(...)`.
- Outbound implementations live under `adapter/out/...` and end with `Adapter` when they implement a port. Technology-specific names are explicit, e.g. `PricingRestAdapter`, `OrderGrpcAdapter`.
- Internal synchronous adapters are grouped by target bounded context and protocol: `adapter/out/internal/<target>/rest|grpc/...`.
- Third-party dependencies use `adapter/out/external/<provider>/<protocol>/...`.
- Inbound service-to-service endpoints use `adapter/in/internal/rest|grpc/...`; provider callbacks use `adapter/in/webhook/...`; Kafka consumers use `adapter/in/messaging/kafka/...`.
- Internal protocol is an architectural/wiring decision, not a runtime `transport` property. Never add `transport: REST|GRPC|...` switching to `application.yml`.
- Service URLs/endpoints are never hard-coded in Java. `application.yml` binds properties to environment variables, while environment-specific values are owned by `ci-cdconfigs/<be-name>/`.

## CI/CD configuration ownership

- `ci-cdconfigs` is part of this repository. Every `be-*` deployable has its own direct child folder; do not create one global endpoint/secrets map for all services.
- Each deployable folder owns local/dev/staging/prod non-secret mapping examples plus Kubernetes base/overlays.
- Real secrets are never committed. Secret example files contain key names/placeholders only; runtime secrets come from protected CI variables, Vault/cloud secret manager, Kubernetes Secret, or External Secrets.
- `application.yml` must not contain default passwords, access keys, tokens, client secrets, or private keys.
- Kubernetes manifests consume per-service ConfigMap/Secret names and remain ready for later Kustomize/External-Secrets integration.

## API rules

- Controllers are interface-driven. Mapping/OpenAPI contract belongs on controller interfaces; implementations implement the interface.
- Public endpoints do not require authentication.
- Authenticated endpoints live under `/private/**` without an `/api/v1` prefix.
- Authorization is permission-based through `@PreAuthorize` and the authorization bean, not hard-coded role checks in controllers.
- Multi-line SpEL expressions use Java text blocks.
- Do not use `@AuthenticationPrincipal` in controllers. Use the request-scoped `UserContext` bean.
- API request/response objects are not JPA entities.
- JavaDoc is required on public/external contracts and on methods whose business semantics, units, ownership, invariants, or error behavior are not obvious from the type/name. Do not add JavaDoc merely to repeat an identifier or signature.
- Private fields, obvious constructors/getters, internal DTO fields, and self-explanatory record components do not require JavaDoc. Record `@param` entries are used only when they add constraints, units, ownership, or other non-obvious semantics.

## Mapping rules

- MapStruct mappers are Spring beans (`componentModel = SPRING`) and are constructor-injected.
- Non-trivial API/application/domain/persistence mapping must use MapStruct instead of repeated manual field copying.
- Inbound adapters must depend on application input-port interfaces (`application/port/in`), never concrete use-case classes.
- Stateless constants/helper holders use Lombok `@UtilityClass`; do not hand-write private-constructor utility classes.
- Do not use static `Mapper.INSTANCE` access.
- Mapping code must not contain business rules.

## Persistence/query rules

- Basic `findBy*`, `existsBy*`, and simple pageable queries use Spring Data repositories.
- Simple relational CRUD/aggregate persistence uses Spring Data JPA.
- Relational search/filter/pagination uses native SQL through Spring Data `@Query(nativeQuery = true)` / `@NativeQuery` first; do not use Specification/Criteria for business search.
- `JdbcClient`/`NamedParameterJdbcTemplate` is reserved for complex/performance-sensitive SQL, atomic locking/CAS, bulk pipelines, ledger/reporting, and vendor-specific outbox/inbox operations with an explicit rationale.
- Spring `Pageable` remains the default pagination abstraction for pageable APIs.
- Search methods use parameter objects/criteria records instead of long parameter lists.
- Do not introduce wrapper value objects for every ID. Plain `Long` identifiers are preferred in criteria/search models unless a richer type protects a real invariant.
- Baseline business schema must not include deliberate performance indexes or partitions. Only indexes implicitly/necessarily created by primary-key/unique integrity constraints are allowed before the performance lab.
- Performance indexes, engine-specific index features, and partitioning are introduced only in dedicated database labs so before/after plans and timings can be compared.
- PostgreSQL partition automation must use `pg_partman`, not hand-written monthly partition-creation jobs.
- Oracle and SQL Server high-volume labs must evaluate engine-appropriate index and partition strategies and preserve before/after execution-plan evidence.
- Every relational schema change MUST be managed by Liquibase. Application code must never create/alter relational tables at runtime.
- Seed DML MUST live in dedicated `.sql` files referenced by Liquibase `sqlFile`; do not embed demo/performance inserts inside schema changeSets.
- Keep schema and seed concerns separate through Liquibase contexts such as `schema`, `seed-demo`, and `seed-large`.
- MongoDB bounded contexts (`be-comment-*`, `be-notification-*`, and review if retained on MongoDB) MUST NOT be wired to MySQL/JPA merely for convenience. Mongo collections/documents remain their source of truth.
- MongoDB has no relational "table Liquibase" requirement; initialize collections/validators/index labs with explicit Mongo bootstrap/migration assets and document the difference from relational Liquibase.

## Java/code-quality rules

- Target Java 25 LTS.
- Use `LocalDateTime` for application/domain/persistence values that contain both date and time; use `LocalDate` for date-only values. Do not introduce legacy `Date`/`Timestamp` domain fields.
- Prefer modern Java features when they improve clarity: records, sealed types where useful, pattern matching, switch expressions, text blocks.
- Do not use language features only for novelty.
- Apply SOLID and Effective Java pragmatically; avoid ceremony without value.
- Avoid long parameter lists; introduce Command/Criteria/Context/Options objects.
- Prefer immutable state and explicit domain methods over public setters.
- Do not use Lombok `@Data` blindly on entities/domain models.
- Spring Boot 4 modules that inject `RestClient.Builder` declare `spring-boot-starter-restclient`; worker/job/outbox modules must not pull MVC/WebFlux starters unless they actually expose HTTP endpoints.
- `spring-boot-starter-test` is always test scoped. Keep imports and dependencies minimal; unused imports and decorative comments are build-quality failures.
- Prefer constructor injection.
- No generic `RuntimeException` for business errors.
- No swallowed exceptions, `System.out`, magic event/topic strings, or ad-hoc `ObjectMapper` creation in business code.

## HTTP input/logging rules

- Every inbound HTTP request gets trace/span context and structured request/response logging.
- Sensitive fields/headers must be masked; credentials/tokens/payment secrets are never logged in clear text.
- String input trimming is centralized via configuration, with explicit opt-out for fields where whitespace is semantically significant.
- Business use cases must never manually trim request fields.

## Kafka rules

- Producers and consumers work with typed objects through shared Kafka serializer/deserializer configuration.
- Business consumers must not manually convert `byte[]` with `ObjectMapper`.
- Event envelope/header conventions carry event id/type/version, correlation/causation identifiers, and W3C trace context.
- Consumers are idempotent. Retry topics/DLT are used according to failure classification.
- DB state + Kafka publication consistency uses the Transactional Outbox pattern where required.
- Duplicate delivery is expected; consumer logic must remain correct under at-least-once delivery.

## Authentication/authorization rules

- Authentication uses Keycloak/OIDC.
- Roles aggregate permissions; application decisions are permission-based.
- Resource-scoped authorization supports seller scope.
- JWT remains intentionally small; full effective permissions/scopes are resolved via authorization APIs/cache.
- Angular loads the authenticated user's roles/permissions/scopes once after login through `/private/me/authorization` and uses them for UX/route guards. Backend always re-enforces authorization.
- HTTP user requests obtain identity from `UserContext`.
- Kafka/scheduler/system executions create an explicit system/service execution context rather than pretending to be a human admin.
- Internal HTTP calls requiring authentication use Keycloak client credentials/service identity.
- Audit differentiates USER, SERVICE, SCHEDULER, and KAFKA_CONSUMER actors.
- Protected admin/security/manual-recovery actions must emit immutable audit history through source outbox-backed events to `be-audit`; audit is not replaced by normal logs.

## Reliability rules

- Never add blind retry.
- Each external dependency defines timeout, retryability, backoff+jitter, circuit breaker, bulkhead, fallback/degradation, and ambiguous-outcome behavior as appropriate.
- Dangerous/ambiguous operations such as payments reconcile/query state before retrying an unknown outcome.
- Saga state must be persistent/recoverable when workflow survival matters.
- Redis use must explicitly declare its role: CACHE, STATE_STORE, LOCK, COUNTER, or RATE_LIMITER.
- Redis object caching MUST use Spring Cache (`@EnableCaching`) with a customized `RedisCacheManager` and typed object serializers; business code must never use `StringRedisTemplate`, `RedisTemplate<String, String>`, or manual `ObjectMapper` String↔object conversion for cached objects.
- Every cache declares an explicit cache name, concrete cached value type, TTL, key prefix, and failure behavior. Rich domain aggregates are not serialized directly when a stable cache DTO/document is more appropriate.
- Cache mutation must not become visible before the source-of-truth transaction commits; Redis cache managers used for transactional data are transaction-aware.
- Async failures must have operational recovery paths: retry, DLT, replay, reconciliation, or manual resolution.
- Payment provider adapters support MoMo, VNPAY, and VietQR behind a provider-neutral port. QR generation alone is never treated as proof of payment.
- Notification is a critical bounded context: persist first/source-of-truth in MongoDB, deliver realtime separately, and isolate large fan-out/background work from query APIs.

## Observability rules

- HTTP, Kafka, scheduler, and internal service calls participate in OpenTelemetry/W3C trace context.
- Structured logs include service, traceId, spanId, request/job/event identifiers, outcome, and latency where relevant.
- Expose meaningful business and technical metrics.
- Observability failure must not normally break core business behavior.

## Testing and evidence

- Backend unit tests are mandatory.
- Integration tests use the real technology via Testcontainers when practical; do not substitute H2 for PostgreSQL/Oracle/MySQL/SQL Server behavior.
- Concurrency, idempotency, outbox, retry/DLT, circuit breaker, Saga recovery, and reconciliation require dedicated tests where used.
- Frontend unit tests are not mandatory by project decision, but Angular must build and real UI flows must be smoke/E2E verified.
- UI evidence must include screenshots of actual running pages and key states.
- Never claim a feature is complete without fresh command/test evidence.

## Use-case learning catalog

- New production-relevant cases discovered during implementation must be documented under `usecase/` before or alongside implementation.
- A use-case note must describe the problem, constraints, candidate solutions, selected solution, trade-offs, failure modes, and how to verify it.
- Use-case notes are decision/learning artifacts; they must not be used to bypass the approved feature spec or implementation plan.
- Important examples include image variants/responsive delivery, database performance evolution, failure recovery, idempotency, cache stampede, ambiguous external outcomes, deployable isolation, seller-change notification fan-out, and audit history.
- Media seed data follows object-storage semantics: binary image objects belong in MinIO/S3-compatible storage; relational databases store only stable metadata/object keys/variant dimensions/content type. Docker seed jobs must make both sides idempotently consistent.
- Large data profiles should generate realistic deterministic synthetic business data in SQL rather than checking millions of literal INSERT rows into Git. Use legally safe local/generated media assets rather than scraped copyrighted product images.

## Documentation

- Every feature has an approved spec before implementation.
- Every significant architectural decision gets an ADR when alternatives/trade-offs matter.
- Security authorization and failure/recovery are delivered in both Markdown and DOCX handbook form.
- Docs must explain why a pattern/design was selected, rejected alternatives, trade-offs, failure modes, and test evidence.

### API search contract rules
- API interfaces contain endpoint annotations and method signatures only; transport DTOs/records must be top-level classes under `api/request/**` or `api/response/**`, never nested inside `*Api` interfaces.
- Search/filter/list endpoints with query criteria accept one grouped `*SearchRequest` or `*QueryRequest` via `@ModelAttribute`; do not expose long lists of loose `@RequestParam` filters. Identity/path selectors that are not search criteria may remain path/context parameters.
- Map transport search requests to application `*SearchCriteria`/`*QueryCriteria` using MapStruct; input/output ports must not depend on web request classes.
- Relational search/filter/pagination binds one grouped criteria object into native Spring Data SQL using SpEL (`:#{#criteria.field}`) instead of many repository `@Param` values. Atomic mutations/locks are exempt because they are commands, not searches.
- The dedicated Search/Discovery bounded context remains OpenSearch-backed; the grouped-request rule still applies to its HTTP API, while the native-SQL rule applies only to relational search/read models.
