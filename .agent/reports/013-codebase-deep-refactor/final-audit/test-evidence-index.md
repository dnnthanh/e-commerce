# Feature 013 — Test and verification evidence index

## Where tests live

- Java unit/architecture/integration test sources: `backend/services/<module>/src/test/`.
- Framework-free executable smoke harnesses: `verification/src/` and `verification/java/`.
- Static/architecture/data/SQL verification scripts: `verification/*.py` and `verification/*.sh`.
- Executed evidence/logs: `.agent/reports/013-codebase-deep-refactor/`.

Current API-module Java test-source inventory: **31 files**.

- `be-audit-api`: 1 test source(s)
- `be-authorization-api`: 2 test source(s)
- `be-cart-api`: 1 test source(s)
- `be-catalog-api`: 2 test source(s)
- `be-checkout-api`: 4 test source(s)
- `be-comment-api`: 1 test source(s)
- `be-fulfillment-api`: 2 test source(s)
- `be-inventory-api`: 2 test source(s)
- `be-media-api`: 1 test source(s)
- `be-notification-api`: 2 test source(s)
- `be-operations-api`: 1 test source(s)
- `be-order-api`: 1 test source(s)
- `be-payment-api`: 1 test source(s)
- `be-pricing-api`: 2 test source(s)
- `be-promotion-api`: 2 test source(s)
- `be-return-api`: 1 test source(s)
- `be-review-api`: 1 test source(s)
- `be-search-api`: 1 test source(s)
- `be-seller-api`: 2 test source(s)
- `be-settlement-api`: 1 test source(s)

## Evidence semantics

- `final-regression.log` in this directory is the fresh source/static/domain/cross-context verification run for the final audit.
- `java25-toolchain.log` records the actual runtime/toolchain commands attempted in this environment.
- A Python/static gate saying a JUnit source exists is **not** a claim that JUnit executed.
- Java-25 `mvn clean verify`, Testcontainers and Docker/HTTP smoke remain pending until a Java-25/network/Docker-capable runtime is available.

## Fresh final-audit execution result

- `verification/run_feature013_final_audit.sh`: **24 commands executed, 24 exited 0, OVERALL=0**.
- `verification/verify_java25_usecase_evidence.py`: **72 passed, 0 failed**.
- Framework-free Java/domain smokes: `PRODUCTION_DEPTH_DOMAIN_SMOKE=PASS`, `CROSS_CONTEXT_WORKFLOW_SMOKE=PASS`.
- Data/SQL labs: PostgreSQL/MySQL/SQL Server/Oracle static lab gates all passed.
- Runtime/toolchain attempt: host Java is 21; Maven Wrapper download failed with DNS exit 6; Docker command is unavailable (exit 127).

