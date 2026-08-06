# Feature 013 — Deep Refactor Checkpoint 1 Verification

**Date:** 2026-08-03  
**Status:** PARTIAL / TOOLCHAIN BLOCKED — do not treat as final spec completion.

## Implemented at this checkpoint

- Backend target moved to Java 25 in `AGENTS.MD`, parent Maven configuration and backend Docker images.
- Lombok upgraded to 1.18.46 for JDK 25 support.
- Added `@UseCase` and `@Persistence` platform stereotypes, both based on Spring `@Component`.
- Added Spotless + Google Java Format verification and JaCoCo report to the parent Maven quality gate.
- Replaced generic `@Service` in bounded-context application implementations with `@UseCase`.
- Replaced generic persistence-adapter `@Repository` with `@Persistence`.
- Safely converted assignment-only constructor injection to Lombok `@RequiredArgsConstructor`; constructors with logic/configuration parameters were intentionally retained.
- Deepened `be-order-api`: multi-seller aggregate, typed statuses/events/reasons, command/query use cases, application/domain/infrastructure exceptions, pageable criteria, JPA entities/repositories/specification adapter, inbox/outbox, optimistic version, API request/response naming and Liquibase schema changes.
- Deepened `be-comment-api`: thread/reply/edit/soft-delete/hide/unhide/report/reaction, typed enums, domain/application exceptions, Mongo documents/repositories/adapter, durable outbox, rate-limit port+adapter, mention extractor, pagination and request/response naming.
- Removed manual `.trim()` and generic `IllegalArgumentException` from Order/Comment domain/application flows.
- Added domain tests for Order and Comment plus platform stereotype tests.
- Added `verification/verify_deep_refactor.py` to provide deterministic structural checks when Maven cannot run.

## Fresh verification evidence

### 1. Structural quality gate

Command:

```bash
python3 verification/verify_deep_refactor.py
```

Result: **PASS — 24 passed, 0 failed**.

The checks cover Java 25 configuration, stereotype definitions, absence of generic `@Service`/persistence `@Repository`, Java type-declaration integrity after automated refactoring, JPA-vs-JDBC policy for Order, no manual trimming/generic argument exceptions in Order/Comment business layers, required bounded-context boundaries, and naming conventions.

### 2. Pure Order domain compilation

Runtime available in this execution environment: OpenJDK 21.0.10.

Command concept:

```bash
javac -d /tmp/ecommerce-domain-check/order \
  $(find backend/services/be-order-api/src/main/java/com/dnnthanh/marketplace/be/order/api/domain \
     -type f -name '*.java' ! -path '*/port/*')
```

Result: **PASS** (`ORDER_DOMAIN_COMPILE=PASS`).

This is a dependency-free syntax check only. It is **not** evidence that the Java 25 Maven build passes.

### 3. Pure Comment domain compilation

Command concept:

```bash
javac -d /tmp/ecommerce-domain-check/comment \
  $(find backend/services/be-comment-api/src/main/java/com/dnnthanh/marketplace/be/comment/api/domain \
     -type f -name '*.java' ! -path '*/port/*')
```

Result: **PASS** (`COMMENT_DOMAIN_COMPILE=PASS`).

This is a dependency-free syntax check only. It is **not** evidence that the Java 25 Maven build passes.

### 4. Domain behavior smoke harness

A temporary dependency-free Java harness exercised:

- multi-seller Order grouping and discount/payable invariants;
- Order payment -> fulfillment -> completed lifecycle;
- completed-order cancellation rejection;
- Comment soft delete preserving reply count;
- mention de-duplication;
- deterministic Comment reaction identity.

Result: **PASS** (`DOMAIN_BEHAVIOR_SMOKE=PASS`).

### 5. Required full quality gate — BLOCKED

Fresh environment evidence:

```text
java -version  -> OpenJDK 21.0.10, exit 0
javac -version -> javac 21.0.10, exit 0
mvn -version   -> command not found, exit 127
./mvnw -version -> file not found, exit 127
docker version -> command not found, exit 127
```

An attempt to refresh/install tooling through the container package manager also timed out because external package repositories are unreachable from this runtime.

Therefore the following required acceptance evidence **has not been produced and must not be claimed**:

- Java 25 `mvn clean verify`;
- Spotless execution against the whole repository;
- JUnit/Mockito test suite execution;
- integration/Testcontainers tests;
- packaging all deployables;
- application startup;
- Docker Compose health;
- HTTP smoke tests.

## Important remaining work before Feature 013 is complete

The project-wide stereotype/Lombok foundation and deep Order/Comment reference implementations are present, but the full spec still requires verified deepening/cleanup of Checkout, Inventory, Payment, Fulfillment, Return, Review, Notification, Seller, Cart, and the remaining bounded contexts. Several legacy Java sources are still compressed and are intended to be normalized by the configured Spotless/Google Java Format gate once Maven can execute.

Because the user explicitly requires build/test evidence, continuing broad mechanical refactors without the Java 25/Maven quality gate would violate the acceptance criteria. The safe next checkpoint is to run this snapshot in an environment with JDK 25 + Maven 3.6.3+ (or add a Maven Wrapper from such an environment), resolve compile/test/format findings, and then continue the remaining waves.
