# Feature 013 — Final service-depth verification

## Result

**Source/service-depth status: PASS for the verification that can execute in this environment.**

**Full runtime acceptance: BLOCKED by environment and therefore not claimed.**

The final regression runs every repository verification script matching `verification/verify_*.py`
and `verification/verify_*.sh` after the final code changes.

```text
verification commands executed: 23
verification commands with non-zero exit: 0
OVERALL=0
```

Key fresh gates include:

```text
Catalog JPA migration:               9 passed, 0 failed
Checkout × Promotion:               16 passed, 0 failed
Checkout resilience/architecture:   16 passed, 0 failed
Outbox crash recovery:              21 passed, 0 failed
Controller → persistence boundary:  PASS
Feature depth:                      59 passed, 0 failed
Feature quality:                    12 passed, 0 failed
Compile/integration-risk source gate:16 passed, 0 failed
Named exception gate:               PASS
Operational hardening:              10 passed, 0 failed
Production-depth domain smoke:      PASS
Cross-context workflow smoke:       PASS
Realistic data + SQL labs:          PASS
Runtime-flow depth source gate:     PASS
Seed integrity/schema drift:        PASS
Final service-depth gate:           40 passed, 0 failed
Typed Redis cache gate:             PASS
Use-case naming gate:               PASS
```

Full raw output is in `final-regression.log`.

## Final service-depth changes

The final pass consolidates bounded contexts around one canonical write path instead of keeping
legacy/demo and production-depth paths side by side.

- **Order**: marketplace parent/seller orders, legal state transitions, seller cancellation,
  unpaid expiry, guarded operations override, Inbox/Outbox and optimistic concurrency.
- **Checkout**: durable process manager, immutable quote, Promotion/Inventory reservations,
  payment ambiguity, compensation and stuck-workflow recovery.
- **Payment**: one canonical Payment aggregate, provider outcome/webhook dedupe, UNKNOWN
  reconciliation and cumulative partial/full refund protection.
- **Fulfillment**: multi-package allocation, over-allocation protection, carrier ordering,
  reconciliation and SLA operations.
- **Return**: one ReturnRequest workflow with partial quantity, approve/reject/inspect/dispute,
  refund and RESTOCK/QUARANTINE/SCRAP disposition.
- **Inventory**: reserve/confirm/release plus adjustment, transfer and reconciliation.
- **Cart**: ACTIVE/SAVED_FOR_LATER, version/idempotency, concurrent first-create handling,
  cache-aside and authoritative Catalog/Pricing/Inventory validation.
- **Promotion**: seller/SKU/category/channel/customer-segment targeting plus usage
  reserve/confirm/release and Checkout compensation.
- **Review/Comment**: canonical review path and production discussion/moderation/reaction flows.
- **Notification**: durable inbox plus atomic multi-instance delivery lease/claim, provider attempt
  idempotency, retry/backoff, dead letter and replay.
- **Settlement**: append-only seller ledger, period lifecycle, hold/dispute and rebuild/reconcile.
- **Authorization**: durable mutation intent before provider calls and reconciliation for incomplete
  provider changes.
- **Operations/Audit/Search/Media/Seller/Pricing/Catalog**: role-specific use cases/read models,
  recovery/audit boundaries and domain-specific persistence decisions.

## Architecture and cleanup evidence

Fresh source audit reports no matches for:

- generic application implementation class names `*Service`;
- controllers directly using `JdbcClient`, `JpaRepository` or `MongoTemplate`;
- production code directly instantiating generic `IllegalArgumentException`,
  `IllegalStateException` or `RuntimeException`;
- stale `PaymentAggregate`, `ReturnCase`, `FulfillmentTransitionPort`, legacy `ReviewService` or
  legacy `Cart` write path;
- dormant `@ConditionalOnBean` production-depth use cases.

Catalog Product ordinary CRUD/search now uses Spring Data JPA + `JpaSpecificationExecutor` +
`@Version`. JDBC is retained only for documented atomic/native/read-model cases; see
`docs/architecture/013-persistence-decisions.md`.

## Data and database learning assets

The realistic seed/data work remains part of the final tree:

- seller/product/SKU long-tail and hot-key skew;
- seasonal order behavior and rare payment/fulfillment failure states;
- reconciliation-friendly cross-service demo scenarios;
- PostgreSQL covering/partial/functional/GIN/GiST/BRIN indexes, extended statistics, keyset,
  LATERAL, window functions, `SKIP LOCKED`, partition pruning and reconciliation queries;
- MySQL, SQL Server and Oracle index/partition/query labs using their own dialects.

## Runtime/toolchain blocker

Fresh toolchain evidence:

```text
Java:   OpenJDK 21.0.10
javac:  21.0.10
mvnw:   exit 6 — could not resolve repo.maven.apache.org
Docker: exit 127 — docker command not found
```

The repository targets Java 25. Because Maven cannot be downloaded and Docker is unavailable in this
execution environment, the following are **not claimed as passing**:

- Java 25 full reactor compilation;
- `./mvnw clean verify`;
- Spotless/ArchUnit/JUnit execution through Maven;
- Testcontainers integration tests;
- Docker Compose startup;
- runtime HTTP/Kafka/database smoke tests;
- real `EXPLAIN ANALYZE` benchmark numbers against the seeded databases.

Those remain the final environment-dependent acceptance gate, rather than unfinished basic service
logic.

## Evidence files

- `final-regression.log` — all 23 fresh repository verification commands.
- `source-audit-final.log` — generic/stale/direct-persistence source scan.
- `toolchain-final.log` — fresh Java/Maven/Docker blocker evidence.
- `docs/architecture/013-production-depth-matrix.md` — final context-by-context service depth.
- `docs/architecture/013-persistence-decisions.md` — explicit JPA/JDBC/Mongo decisions.
