# Feature 013 — Persistence decision matrix

The repository follows one deterministic rule so later use cases do not drift:

1. **Simple relational aggregate CRUD -> Spring Data JPA.**
2. **Relational search/filter/pagination -> native SQL**, preferably Spring Data `@Query(nativeQuery = true)` / `@NativeQuery` with projections.
3. **JdbcClient/JdbcTemplate -> only complex/optimized/vendor-specific database work** where explicit SQL control is the reason for the adapter: atomic conditional mutation, lock/claim semantics, bulk/merge, ledger/reporting, or vendor-specific outbox/inbox SQL.
4. **Mongo-owned bounded contexts -> Spring Data MongoDB / MongoTemplate only where document atomic operations justify it.**

`JpaSpecificationExecutor`, Criteria API and long derived-query chains are not used for business search.

## JPA-first relational aggregates

| Context | JPA responsibility | Search/read responsibility |
| --- | --- | --- |
| Catalog Product | Product CRUD + optimistic `@Version` | native SQL product search; JDBC only for the compact SKU join snapshot and PostgreSQL JSONB outbox fragment |
| Cart | Cart aggregate CRUD/versioning | native SQL only when a relational cart search is introduced; Redis remains cache-aside, not source of truth |
| Pricing | Price-rule CRUD/history | native SQL effective-price resolution by seller/channel/time priority |
| Promotion | Campaign/condition candidate persistence | native SQL candidate search; JDBC only for race-sensitive usage reserve/confirm/release |
| Review | Review CRUD/helpful state | native SQL published list and rating summary |
| Seller | Shop/seller/staff CRUD | native SQL seller/shop/staff list projections; JDBC fragments only for vendor JSON history/outbox |
| Media metadata | Media lifecycle CRUD + optimistic version | native SQL variant projection; JDBC fragment only for vendor JSON outbox |
| Authorization mutation journal | mutation-intent CRUD + outbox inserts | native SQL deterministic pending-reconciliation search |
| Order | marketplace/seller-order aggregate CRUD + optimistic concurrency | native SQL order search/filter/pagination |
| Return | canonical Return aggregate/lines | native SQL return list/search; workflow recovery SQL stays isolated when it needs explicit coordination |

## Deliberately retained JDBC

| Adapter / area | Why JDBC remains |
| --- | --- |
| `JdbcSkuSnapshotAdapter` | compact Product + SKU join projection for an internal authoritative snapshot; no aggregate hydration |
| `ProductPersistenceAdapter` JDBC fragment | PostgreSQL `jsonb_build_object` transactional-outbox insert; Product CRUD/search itself is JPA/native repository SQL |
| `JdbcAuditQueryPersistenceAdapter` | append-only operational reporting/search with explicit SQL projection |
| Checkout process/saga adapters | durable process-manager checkpoints, idempotency and recovery state |
| `JdbcShipmentRepositoryAdapter` | carrier callback sequencing, vendor lock semantics, multi-package allocation/reconciliation |
| `JdbcInventoryRepositoryAdapter` | atomic reserve/release/transfer/adjustment and contention-sensitive stock invariants |
| `JdbcOperationsPersistenceAdapter` | incident/recovery reporting, guarded claim/replay and operations queries |
| `JdbcPaymentPersistenceAdapter` | provider inbox dedupe + state transition + outbox must be coordinated in one explicit transaction |
| `JdbcPromotionUsageAdapter` | race-safe global/customer usage reservation and compensation |
| `JdbcReturnWorkflowPersistenceAdapter` | distributed return/refund recovery bookkeeping separate from ordinary Return JPA aggregate persistence |
| Settlement JDBC adapters | immutable ledger append, period close, payout claim and reconciliation/aggregation queries |
| Outbox workers | `SKIP LOCKED`, `READPAST`, lease/claim/backoff/DLQ SQL; these are explicitly database-control workloads |

## Mongo-owned contexts

Comment and Notification keep MongoDB as durable source of truth because thread/reply documents,
reaction/report atomic updates, inbox documents and delivery lease/claim semantics fit Mongo atomic
operations. The relational JPA/native-SQL rule does not force these contexts into SQL.

## Review checklist for any new JDBC use

A new JDBC adapter is rejected unless all questions are answered:

1. What concrete database-specific, atomic, bulk, reporting or measured performance requirement makes JPA/native repository SQL insufficient?
2. Why is the SQL isolated in an outbound adapter rather than application/domain code?
3. Which test or database lab demonstrates the behavior that motivated explicit JDBC control?

If the answer is merely “the SQL is easy to write”, use Spring Data JPA instead.
