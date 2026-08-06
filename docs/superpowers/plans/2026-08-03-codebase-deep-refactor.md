# Codebase Deep Refactor Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Upgrade the backend to Java 25 and perform the approved project-wide refactor foundation plus deep Order and Comment bounded-context implementations with reproducible quality/build/test evidence.

**Architecture:** Keep the repository's current multi-module bounded contexts, add shared application/persistence stereotypes in the platform starter, use JPA for basic relational persistence, keep MongoDB for comments, and deepen business behavior through explicit domain policies/state transitions and ports. Apply the new conventions first, then refactor Order and Comment as reference implementations before propagating lightweight convention changes to the remaining modules.

**Tech Stack:** Java 25, Spring Boot 4.1, Spring Data JPA, Spring Data MongoDB, Lombok, MapStruct, Bean Validation, Maven, Spotless, JaCoCo, JUnit 5, ArchUnit.

## Global Constraints

- Java 25 is the single project target in `AGENTS.MD`, `.agent/CONVENTIONS.MD`, spec 013, Maven compiler configuration, and Docker backend image.
- `@UseCase` and `@Persistence` are meta-annotated with `@Component` and used only for their defined roles.
- Use Lombok `@RequiredArgsConstructor` for Spring components with required final collaborators.
- JPA is the default for basic relational CRUD/search; JDBC remains only for complex/native/performance-sensitive cases with rationale.
- MongoDB remains the source of truth for comments.
- Business closed sets are enums; repeated technical literals become constants/configuration.
- Exceptions are separated by domain/application/infrastructure/API concern.
- Modified code must be formatted and compile without avoidable warnings.
- Completion claims require fresh build/test/start/smoke evidence.

---

### Task 1: Java 25 and shared quality foundation

**Files:**
- Modify: `AGENTS.MD`
- Modify: `.agent/CONVENTIONS.MD`
- Modify: `.agent/specs/013-codebase-deep-refactor.md`
- Modify: `backend/pom.xml`
- Modify: `Dockerfile.backend`
- Create: `backend/platform/be-platform-starter/src/main/java/com/dnnthanh/marketplace/be/platform/stereotype/UseCase.java`
- Create: `backend/platform/be-platform-starter/src/main/java/com/dnnthanh/marketplace/be/platform/stereotype/Persistence.java`
- Create: platform stereotype tests.

**Interfaces:**
- Produces: `@UseCase`, `@Persistence` available to every backend module.

- [ ] Write tests asserting both annotations are runtime-visible, type-targeted, documented and meta-annotated with `Component`.
- [ ] Update all Java version declarations to 25 and remove the 21/25 contradiction.
- [ ] Add deterministic Spotless formatting and JaCoCo test reporting to the parent build without forcing unrelated database integration tests.
- [ ] Run platform starter tests and formatter check.

### Task 2: Order domain model and application contracts

**Files:**
- Replace/refactor: `backend/services/be-order-api/src/main/java/.../domain/model/MarketplaceOrder.java`
- Create: order enums, domain exceptions, cancellation policy, commands, queries, response DTOs, repository port.
- Create: domain/application tests.

**Interfaces:**
- Produces: typed state transitions, create/cancel/payment/fulfillment commands and `OrderRepositoryPort` contract.

- [ ] Write failing state-transition/idempotency/cancellation tests.
- [ ] Implement enums and domain exceptions.
- [ ] Implement aggregate transition methods with optimistic version semantics.
- [ ] Implement command/query records and application port signatures.
- [ ] Run focused order domain tests.

### Task 3: Order JPA persistence and use cases

**Files:**
- Replace: `JdbcOrderRepositoryAdapter.java`
- Create: `OrderJpaEntity`, `OrderLineJpaEntity`, `OrderJpaRepository`, specifications/projections, mapper, `OrderPersistenceAdapter`.
- Refactor: `OrderApplicationService` into named use-case components using `@UseCase` and Lombok.
- Modify: `be-order-api/pom.xml` to use Spring Data JPA.
- Update Liquibase schema for version/idempotency/audit fields where required.
- Create repository and use-case tests.

**Interfaces:**
- Consumes: Task 2 contracts.
- Produces: transactional order creation, search, cancellation, payment and fulfillment handling.

- [ ] Write failing persistence/use-case tests.
- [ ] Replace basic JDBC CRUD with JPA adapter.
- [ ] Add pageable criteria search through `JpaSpecificationExecutor`.
- [ ] Add application use cases with explicit transaction boundaries and domain exceptions.
- [ ] Run order module tests.

### Task 4: Comment domain depth and Mongo persistence

**Files:**
- Refactor existing comment domain/application/mongo files.
- Create: status/reaction/report enums, domain exceptions, policy classes, request/response/search records, persistence port, Mongo documents/repositories/adapter.
- Create tests.

**Interfaces:**
- Produces: create/reply/edit/soft-delete/moderate/report/react/search operations while preserving Mongo ownership.

- [ ] Write failing domain tests for reply, edit ownership, soft delete with descendants, moderation and idempotent reactions.
- [ ] Implement typed comment domain models and policies.
- [ ] Implement Mongo repositories/adapter annotated `@Persistence`.
- [ ] Implement `@UseCase` application services with Lombok constructor injection.
- [ ] Run comment tests.

### Task 5: HTTP/API cleanup for Order and Comment

**Files:**
- Refactor order/comment controllers and API contracts.
- Create request/response/search request packages and API mappers.
- Extend centralized exception translation/error codes.
- Create controller contract tests.

**Interfaces:**
- Consumes: Order and Comment use cases.
- Produces: validated, role-clear HTTP contracts with stable errors.

- [ ] Write failing MVC contract tests.
- [ ] Replace ambiguous transport/domain sharing with request/response DTOs.
- [ ] Add validation and stable error mapping.
- [ ] Run MVC tests.

### Task 6: Convention propagation to remaining service classes

**Files:**
- Modify application services/persistence adapters across backend modules where changes are mechanical and safe.

**Interfaces:**
- Produces: consistent Lombok constructor injection, `@UseCase`/`@Persistence`, formatted source, obvious enum/constants where closed sets already exist.

- [ ] Inventory `@Service`, manual constructors, generic business exceptions and obvious duplicated literals.
- [ ] Apply stereotype/Lombok changes only where role is unambiguous.
- [ ] Do not migrate complex JDBC blindly; document retained cases.
- [ ] Run formatter and compile affected modules.

### Task 7: Architecture and quality regression tests

**Files:**
- Create architecture tests in platform/order/comment test sources.
- Add static-quality verification scripts/report directory.

**Interfaces:**
- Produces: executable checks for stereotypes, package boundaries, forbidden field injection/manual business trimming in refactored contexts.

- [ ] Write failing ArchUnit checks.
- [ ] Fix violations in Order/Comment/platform.
- [ ] Run formatter, unit tests, architecture tests and package build.

### Task 8: Runtime verification and evidence

**Files:**
- Create: `.agent/reports/013-codebase-deep-refactor/wave-1-3/verification.md`
- Update: spec status/evidence section if successful.

**Interfaces:**
- Produces: reproducible command/output evidence.

- [ ] Ensure Java 25 and Maven are available; add Maven Wrapper if needed.
- [ ] Run `./mvnw -f backend/pom.xml spotless:check test package` (or equivalent verified Maven invocation).
- [ ] Start affected deployables with required local infrastructure or test profile.
- [ ] Smoke representative Order and Comment APIs.
- [ ] Record exact commands, exit codes, test counts and blockers in the verification report.

## Execution checkpoints 4–6

- [x] Checkpoint 4: Checkout -> Promotion reserve/confirm/compensate integration with authenticated
      internal boundary and usage-limit persistence metadata.
- [x] Checkpoint 5: typed Checkout remote configuration, CircuitBreaker/Bulkhead isolation,
      architecture-rule source and failure-path test source.
- [x] Checkpoint 6: Order/Payment outbox claim lease, crash recovery, retry/backoff, terminal FAILED
      delivery state and schema migrations.
- [x] Checkpoint 7: deterministic production-shaped demo/large seed distributions, one authoritative
      `data/` seed source, schema-drift/static integrity gates, cross-service scenario fixtures, and
      advanced index/partition/reconciliation SQL labs across PostgreSQL/MySQL/SQL Server/Oracle.
- [ ] Final Java 25 Maven/Spotless/JUnit/ArchUnit/Testcontainers/Docker evidence — blocked by the
      current execution environment; do not mark Feature 013 complete without it.
