# Feature 013 — Checkpoint 7 verification

## Scope

Checkpoint 7 focuses on two requests: production-shaped seed data and advanced SQL/index/partition
learning labs. It does not claim the final Feature 013 runtime gate.

## Implemented

- Reworked demo data to express readable business scenarios instead of repeated modulo clones:
  varied cart selection/save-for-later, valid checkout states, payment UNKNOWN/FAILED/refund cases,
  reconciliation-safe Order monetary allocations, multi-carrier/partial Fulfillment and cross-service
  scenario documentation.
- Reworked large generators with hot/cold sellers and SKUs, long-tail demand, status rarity,
  seasonality/recency, business-hour peaks, rating bias, stockouts and ledger/settlement adjustments.
- Replaced the MySQL 1M-review procedural row loop with set-based generation.
- Removed unreferenced service-local performance seed copies; `data/` is now the single seed source.
- Fixed seed defects discovered by the new gates: demo Order missing required `checkout_key`, Oracle
  large seed Compose filename mismatch, missing OpenSearch seed scripts, SQL Server Order monetary
  inconsistency, and a Checkout condition-order bug that previously shadowed PAYMENT_UNKNOWN.
- Added expected distribution/selectivity documentation for planner exercises.
- Added PostgreSQL covering/partial/functional/GIN/GiST/BRIN/extended-statistics/keyset/LATERAL/window/
  SKIP LOCKED/range-partition/pruning and reconciliation labs.
- Added MySQL index/window/monthly partition labs, SQL Server INCLUDE/filtered index/partition/APPLY/
  monetary reconciliation labs, and Oracle function-based/local-partition/interval/analytics/ledger
  reconciliation labs.
- Fixed `verify_database_assets.py` to resolve Liquibase `sqlFile` correctly for both
  `relativeToChangelogFile: true` and `false`.

## TDD/evidence

RED evidence exists for missing realism/SQL assets, seed schema/integrity issues, case-study labs,
condition shadowing and duplicate legacy seed sources. The corresponding gates were then rerun GREEN.

Fresh final regression is stored in `final-regression.log`. Key summaries:

```text
REALISTIC_DATA_SQL_LABS=PASS
SEED_INTEGRITY=PASS
SCHEMA_DRIFT_STATIC=PASS
SET_BASED_LARGE_SEEDS=PASS
DEMO_BUSINESS_DIVERSITY=PASS
LIQUIBASE_CHANGELOGS_PASS=17
MEDIA_SEED_FILES_PASS=300
RUNTIME_FLOW_DEPTH_PASS
Feature 013 quality: 12 passed, 0 failed
Feature 013 depth: 59 passed, 0 failed
Active use cases: 14 passed, 0 failed
Checkpoint 4: 16 passed, 0 failed
Checkpoint 5: 16 passed, 0 failed
Checkpoint 6: 21 passed, 0 failed
SHELL_SYNTAX=PASS
YAML_PARSE=PASS
PRODUCTION_DEPTH_DOMAIN_SMOKE=PASS
CROSS_CONTEXT_WORKFLOW_SMOKE=PASS
FULL_REGRESSION_EXIT=0
```

## Runtime benchmark status

No database execution-plan latency/buffer numbers are fabricated. The current environment has Java
21, not Java 25; Maven Wrapper cannot download Maven because `repo.maven.apache.org` cannot be
resolved; Docker is not installed. Therefore Java-25 reactor build, Testcontainers, actual seed load,
`EXPLAIN ANALYZE` runtime comparison and Docker smoke remain BLOCKED here. See `toolchain.log`.

The SQL labs deliberately provide baseline and candidate statements so these measurements can be
captured later in an environment with the database engines available.
