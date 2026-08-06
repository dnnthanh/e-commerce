# Feature 013 — Final service-depth matrix

Feature 013 treats a bounded context as production-depth only when it has meaningful invariants,
concurrency/idempotency decisions, failure/recovery behavior and explicit integration boundaries. A
large number of classes is **not** considered depth by itself.

| Context | Canonical business depth | Concurrency / idempotency / recovery | Main patterns / boundaries |
| --- | --- | --- | --- |
| Catalog | product lifecycle, dynamic attributes, variant generation, checkout SKU snapshot, publish only when media is ready | JPA `@Version`; stale Product write rejected; source outbox | Aggregate, Specification, JPA repository, projection port, Outbox |
| Media | upload -> scan -> process -> ready/failed, checksum dedupe, deterministic variants, safe deletion/reference checks | media+variant idempotency; failed processing retry path | State, Adapter, Worker, Outbox |
| Search | catalog/review projection, stale source-version rejection, filters/facets, cursor/search-after | duplicate/older event cannot overwrite newer projection; full/incremental reindex assets | CQRS read projection, Idempotent Consumer |
| Pricing | seller/channel/time effective price, immutable checkout quote, priority resolution | overlapping rule conflict resolved deterministically | Strategy, Specification-like resolver, immutable snapshot |
| Promotion | seller/SKU/category/channel/customer-segment targeting, stacking/exclusion, explainable eligibility, checkout reserve/confirm/release | global/per-customer usage limit; reservation idempotency; checkout compensation | Specification, Chain/priority policy, Reservation |
| Inventory | available/on-hand/reserved ledger, atomic reserve, confirm/release, adjustment, transfer, reconciliation | last-unit race protection, unique operation reference, duplicate event handling | Reservation, atomic DB mutation/lock, Inbox/Outbox, reconciliation |
| Cart | ACTIVE/SAVED_FOR_LATER, guest merge, versioned mutation, multi-seller lines, authoritative checkout validation | optimistic cart version; concurrent create winner; idempotent mutation; cache-aside | Aggregate, optimistic concurrency, cache-aside, remote validation ports |
| Checkout | durable process snapshot, price/promotion/inventory/payment/order orchestration, compensation, stuck-workflow recovery | idempotency key; compensation after partial success; PAYMENT_UNKNOWN is recoverable state | Saga / Process Manager, State, Circuit Breaker, Bulkhead |
| Order | parent marketplace order + seller orders + lines, state machines, seller cancel, auto-expire unpaid, guarded ops override, payment/fulfillment inbox | exactly-once create by checkout key; optimistic version; duplicate/out-of-order integration handling | Aggregate, State, Inbox/Outbox, JPA Specification/query model |
| Payment | payment attempts, provider result, UNKNOWN reconciliation, webhook dedupe, cumulative partial/full refunds | provider event Inbox; callback race safe; cumulative refund cap | Provider Strategy/Adapter, State, Idempotent Consumer, reconciliation |
| Fulfillment | multi-package allocation, carrier assignment, partial shipment, tracking ordering, SLA, Order-vs-Shipment reconciliation | no over-allocation across packages; carrier sequence rejects stale webhook; idempotent materialization | State, Adapter, reconciliation, Inbox/Outbox |
| Return | partial quantities, approve/reject, receive/inspect, dispute, resolution, refund, RESTOCK/QUARANTINE/SCRAP | cumulative quantity protection; refund idempotency; disposition event emitted only on inspection transition | Saga/state workflow, immutable Order snapshot, compensation/recovery |
| Review | verified-purchase eligibility, edit window/history, moderation, helpful/report semantics | one canonical ProductReview path; helpful/report duplicate-safe | Policy, State, event-driven rating projection |
| Comment | root/reply/edit, soft delete preserving children, hide/unhide, report, reaction, mention, pagination, anti-spam/rate-limit | Mongo atomic mutation/transaction where supported; concurrent reaction/report retry safety | Policy, Mongo source of truth, Outbox, cursor pagination |
| Notification | durable inbox, preferences/quiet-hours, provider delivery, retry/backoff, DLQ, replay, realtime acceleration | atomic Mongo `findAndModify` lease/claim prevents multi-instance double-send; provider attempt idempotency | Adapter, durable work queue, Retry/DLQ, lease/claim |
| Seller | onboarding/verification/suspension, staff/resource scope, public Shop profile | canonical SellerAccount lifecycle, seller isolation and privileged audit | Aggregate State, ownership policy |
| Authorization | role/seller-scope changes against provider with durable mutation intent | intent recorded before Keycloak call; incomplete provider mutation is reconciled | Durable command intent, Adapter, recovery/reconciliation |
| Audit | append-only business audit, redaction, actor/action/resource/time query | event ingestion duplicate-safe; read model is query-oriented | Append-only log, reporting projection |
| Settlement | immutable seller ledger, sale/commission/refund entries, close/hold/dispute, rebuild/reconcile | duplicate source event suppression; zero-ledger period reconciliation preserved | Ledger, Idempotent Consumer, reconciliation |
| Operations | stuck workflow search, guarded replay/recovery, kill-switch/reconciliation commands | request-key idempotency; no arbitrary controller-to-row repair | Command, audit, recovery workflow |

## Scope audit note

This matrix describes the canonical **core** flows that are already deep enough to teach the intended patterns. It is not a claim that every secondary bullet in Spec 013 exists. The remaining mandatory gaps are tracked explicitly in [`usecase/feature-013-gap-register/README.md`](../../usecase/feature-013-gap-register/README.md).


## Cross-context failure scenarios

Framework-free smoke verification currently executes these business invariants independently of
Spring infrastructure:

1. Cart -> Checkout -> promotion/inventory reservation -> ambiguous payment -> compensation.
2. Two contenders reserve the final Inventory unit: only one succeeds and stock never goes negative.
3. Concurrent Payment result paths with the same provider event: one business transition/effect wins.
4. Multi-package Fulfillment -> partial Return -> refund/Settlement adjustment.
5. Outbox publisher crashes after broker send but before marking processed -> duplicate delivery is
   harmless because downstream processing is idempotent.

These are source/domain proofs. They intentionally do not replace the Java 25 Maven,
Testcontainers, Docker Compose and HTTP runtime acceptance gate.

## V11 runtime, frontend and DBA depth evidence

Production depth is evaluated beyond backend class count. The runtime and learning surface must also
exercise the bounded contexts realistically.

| Surface | Depth expectation | Current evidence |
| --- | --- | --- |
| Storefront | product/media/price, cursor search, cart optimistic versioning, checkout, order/shipment, return, review/comment, notification/account | typed `MarketplaceApiService`, cart-driven checkout, order detail, return/dispute, community and notification flows |
| Admin / seller operations | catalog/media/pricing/promotion, inventory, order/fulfillment, payment, return inspection/refund, moderation, seller, settlement, authorization/audit/operations | dedicated feature components and guarded routes; browser code never calls `/internal/**` |
| Compose runtime | a root command runs the full stack; split compose files must use repo-root paths and never traverse `..` | scoped `./backend` and `./frontend` build contexts, `compose-up.sh`, compose path regression verifier |
| PostgreSQL DBA labs | planner/cardinality, MVCC, locks, WAL/buffers, partitioning, index budget, keyset, JSON/GiST/GIN | 8 deep incidents plus advanced/production case packs |
| MySQL DBA labs | optimizer/index prefix, InnoDB locks/undo, filesort/temp, partitioning, generated indexes, keyset | 8 deep incidents with `EXPLAIN ANALYZE`, performance_schema/sys evidence |
| SQL Server DBA labs | parameter sensitivity, key lookup tipping, memory grants/tempdb, deadlocks, partitioning, Query Store, columnstore | 8 deep incidents with execution-plan/DMV/Query Store evidence |
| Oracle DBA labs | bind peeking/ACS, clustering factor, PGA/TEMP, SKIP LOCKED, partition indexes, UNDO, MV rewrite, plan baselines | 8 deep incidents with `DBMS_XPLAN`, `V$`/`DBA_*` evidence |

The database labs deliberately require baseline evidence, competing solutions, regression matrices,
write/maintenance trade-offs and an explicit production decision. They are not syntax tutorials.
