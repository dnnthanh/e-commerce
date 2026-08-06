# 015 - Frontend Runtime, Compose Boundaries, and DBA Lab Depth

## Status

Accepted for feature-013 hardening snapshot v11.

## Problem

The backend had grown into many bounded contexts while the Angular applications remained a thin API demo. Database labs listed advanced keywords but several production cases were too short to train plan diagnosis and production trade-off reasoning. Docker Compose also used broad or parent-relative build contexts that were fragile under macOS path/privacy rules and multi-file Compose resolution.

## Runtime decision

The canonical command remains:

```bash
docker compose up -d --build
```

Application build contexts are scoped:

```text
backend services -> ./backend -> backend/Dockerfile.service
storefront       -> ./frontend -> frontend/Dockerfile.storefront
admin            -> ./frontend -> frontend/Dockerfile.admin
```

Compose fragments use root-relative paths and are invoked with `--project-directory .`. Parent build contexts and parent-relative bind/env paths are forbidden by CI. Split files share the explicit `marketplace-network` name without `external: true`, so multi-file Compose can create/merge the network deterministically. `compose-up.sh` explicitly pins the root compose and clears accidental `COMPOSE_FILE` state.

This separates a repository/configuration bug from macOS Docker Desktop permission to folders such as Downloads.

## Frontend architecture

`ApiService` owns transport/envelope/error/trace concerns. `MarketplaceApiService` owns backend route composition and typed bounded-context methods. Components own UI state and orchestration only; they do not parse envelopes or call service-internal routes.

### Storefront bounded-context graph

```text
Discovery -> Catalog + Search
Product 360 -> Catalog + Pricing + Media + Review + Comment + Seller subscription
Cart -> Cart validation/version
Checkout -> Cart lines -> Checkout saga -> Pricing/Promotion/Inventory/Order/Payment server-side
Order detail -> Order + Fulfillment
Return -> Return/Refund lifecycle
Notification -> durable API + realtime stream
```

### Admin graph

```text
Seller / Catalog / Media / Pricing / Promotion
Inventory / Order / Fulfillment / Payment / Return
Settlement / Security / Audit / Operations / Moderation triage
```

The frontend intentionally does **not** call `/internal/**`. If an operator workflow exists only as an internal endpoint, that is a backend contract gap and must be solved by adding a permission-protected admin/private API rather than leaking service credentials/routes to the browser.

## Known frontend contract gap

The public Product response has product-level data but no complete public SKU/offer projection. A real ecommerce product page should not ask users for SKU ids. The next catalog API evolution should expose sellable variants/offers containing at minimum SKU id, seller/offer identity, display attributes and a price/inventory resolution key. Until that API exists, the demo keeps SKU input explicit and does not use the internal SKU owner endpoint.

## DBA lab decision

Database labs now have eight maturity levels. `08-deep-incidents` is the production DBA tier and exists for all four relational engines in the platform.

Each deep incident requires:

- millions-of-rows or equivalent realistic data shape;
- a deliberate hot-key/long-tail skew where relevant;
- baseline actual-plan capture;
- engine-native evidence views/DMVs;
- root-cause hypothesis and falsification;
- multiple competing solutions;
- read/write/storage/concurrency trade-off;
- regression matrix;
- acceptance criteria and rollback;
- final `ACCEPT`, `REJECT` or `TEMPORARY MITIGATION` decision.

### PostgreSQL focus

Cardinality/extended statistics, CTE/join fan-out, `SKIP LOCKED`, MVCC/autovacuum/visibility map, sort/hash spill, keyset under concurrency, GIN/GiST operator classes, BRIN/partition/index budget.

### MySQL focus

Leftmost-prefix and histograms, covering versus clustered/wide-row fetch, filesort/temp spills, InnoDB next-key/gap locks, keyset, partition pruning, JSON generated-column indexing, undo/purge lag.

### SQL Server focus

Parameter sniffing/PSP, Key Lookup tipping, memory grant spill/feedback, tempdb version store, deadlock/lock escalation, partition elimination/aligned indexes, Query Store regression, columnstore hybrid.

### Oracle focus

Bind peeking/adaptive cursor sharing, clustering factor/rowid access, PGA/TEMP workareas, `SKIP LOCKED`, local/global partition indexes, UNDO/ORA-01555, materialized view rewrite, invisible indexes/SQL plan management.

## Verification

- `verify_compose_runtime_paths_v11.py`
- `verify_frontend_backend_coverage_v11.py`
- `verify_frontend_typescript_syntax_v11.js`
- `verify_database_lab_depth_v11.py`

These gates test structure and contract alignment; actual Docker runtime, Angular dependency build and database execution evidence still need the relevant local runtimes.
