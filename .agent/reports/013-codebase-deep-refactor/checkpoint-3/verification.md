# Feature 013 — Checkpoint 3 Verification

## Scope implemented in this checkpoint

Checkpoint 3 converts the previously dormant production-depth use cases into concrete, wired flows.
It is **not** the final Feature 013 acceptance result because the execution environment cannot run the
Java 25 Maven reactor or Docker/Testcontainers.

### Concrete production-depth adapters activated

- `StockLedgerPort` — inventory reservation/confirm/release with transaction/locking semantics.
- `CheckoutProcessPort` — PostgreSQL durable process-manager state and idempotency key.
- `ProductReviewPort` — verified-purchase review moderation/helpful state persistence.
- `SettlementLedgerPort` — Oracle append-only seller ledger + inbox deduplication.
- `ProductSearchPort` — OpenSearch search-after cursor/facets/stale-event aware search adapter.
- `ShipmentRepositoryPort` — SQL Server shipment aggregate + atomic carrier callback claim.
- `SellerAccountPort` — seller lifecycle/staff persistence.
- `CartRepositoryPortV2` — cart version/CAS persistence and selected-line state.
- `MediaAssetPort` — PostgreSQL media processing state/checksum/variant idempotency.
- `PaymentAggregatePort` — PostgreSQL provider callback inbox + payment aggregate transition/outbox.
- `NotificationInboxPort` — Mongo durable provider-delivery deduplication/preferences.
- `PromotionUsagePort` — concurrency-safe promotion usage reservation.
- `ReturnCasePort` — PostgreSQL return inspection/accepted quantity/refund snapshot persistence.

All `@ConditionalOnBean` gates that previously disabled these use cases were removed after concrete
adapters existed.

### Additional architecture changes

- Notification HTTP controller no longer owns `MongoTemplate`; it calls `NotificationInboxUseCase`.
- Notification Mongo adapter now owns inbox queries, read markers, preferences, seller follows and
  provider-delivery deduplication.
- Media upload requires a checksum and creates a durable `MediaAsset` processing state.
- Shipment carrier callbacks use an atomic idempotency-key claim instead of check-then-mark.
- Settlement event inbox + ledger append run in one application transaction.
- Payment callback loads the target payment before claiming the provider event, then writes state +
  outbox through a persistence adapter.
- Return inspection persists accepted quantity per line instead of only computing a transient total.

## Fresh verification evidence

### Active production-depth ports

Command:

```bash
python3 verification/verify_feature_013_active_usecases.py
```

Result: **14 passed, 0 failed**. See `active-usecases.log`.

### Static architecture/quality gate

Command:

```bash
python3 verification/verify_feature_013_quality_gate.py
```

Result: **12 passed, 0 failed**. See `quality-gate.log`.

### Production-depth inventory

Command:

```bash
python3 verification/verify_feature_013_depth.py
```

Result: **59 passed, 0 failed**. See `depth-verification.log`.

### Framework-free domain/cross-context execution

Command:

```bash
verification/verify_production_depth_smokes.sh
```

Result:

```text
PRODUCTION_DEPTH_DOMAIN_SMOKE=PASS
CROSS_CONTEXT_WORKFLOW_SMOKE=PASS
```

The smoke executes real Java domain behavior for fulfillment, pricing, promotion, inventory, cart,
checkout compensation, payment callback/refund invariants, return inspection, review moderation,
seller permissions, search stale-event protection, notification quiet-hour/dedup, settlement ledger,
catalog dynamic attributes and media processing. It also executes oversell race, payment callback
race, partial fulfillment/return/settlement adjustment and outbox duplicate-delivery semantics.

### Changed domain source compilation

The modified framework-independent domain classes were compiled directly with the available `javac`
and completed successfully:

```text
DOMAIN_COMPILE_CHANGED=PASS
```

This is syntax/domain evidence only; it is not a substitute for the Java 25 Maven reactor.

## Toolchain blocker

Fresh environment evidence is recorded in `toolchain-environment.log`.

The execution container currently provides:

- OpenJDK/Javac 21.0.10.
- No system Maven.
- No Docker CLI/daemon.
- Maven Wrapper bootstrap cannot resolve `repo.maven.apache.org` because outbound DNS/network from the
  execution container is unavailable.

Therefore the following final acceptance items remain **BLOCKED, not passed**:

- Java 25 reactor compile.
- `./mvnw -f backend/pom.xml clean verify`.
- Spotless execution against the complete reactor.
- JUnit/ArchUnit/Testcontainers suites through Maven.
- Docker Compose startup.
- Runtime API smoke against real PostgreSQL/MySQL/SQL Server/Oracle/Mongo/Redis/Kafka/OpenSearch.

Feature 013 must remain `Implementation in progress` until these commands execute successfully in a
Java 25 + Maven + Docker capable environment.
