# Feature 013 — Final Java 25 / use-case / test-evidence audit

## Java baseline

Repository configuration is aligned to **Java 25**:

- root `.java-version` = `25`;
- parent Maven `<java.version>25</java.version>` and compiler `release=${java.version}`;
- Maven Enforcer requires `[25,26)` and fails fast on the wrong JDK;
- backend build image = `maven:3.9.11-eclipse-temurin-25`;
- backend runtime image = `eclipse-temurin:25-jre`;
- Kafka JMX exporter runtime image = `eclipse-temurin:25-jre`;
- AGENTS/spec/conventions now describe Java 25 as the single baseline.

## Use-case audit

All 20 API bounded contexts have one or more `@UseCase` application entry points and at least one backend test source. The core transaction-heavy flows are no longer CRUD wrappers: Checkout Saga, Inventory reservation/reconciliation, Order lifecycle, Payment UNKNOWN/refund, multi-package Fulfillment, Return inspection/dispute/disposition, Notification retry/DLQ/replay, Authorization recovery, Settlement ledger and others are represented in source.

However, the complete Production-depth amendment is **not fully closed**. Secondary mandatory use cases still open are intentionally documented in `usecase/feature-013-gap-register/README.md`, and per-context status is in `usecase-coverage.md`. This prevents a false “complete” claim.

## Discovered use cases

The final audit added dedicated learning notes for the production-relevant cases introduced during Feature 013, including Checkout Saga compensation, oversell/reconciliation, webhook idempotency/refund, multi-package fulfillment, partial-return disposition/dispute, promotion targeting/reservation, notification retry/DLQ/replay, durable authorization mutation recovery, and settlement reconciliation. The top-level `usecase/README.md` indexes all notes.

## Test/evidence structure

- Java test source: `backend/services/<module>/src/test/`.
- Framework-free smoke code: `verification/src/` and `verification/java/`.
- Static/data/architecture verification: `verification/*.py` and `verification/*.sh`.
- Executed evidence: `.agent/reports/013-codebase-deep-refactor/`.
- Current final audit entry point: `.agent/reports/013-codebase-deep-refactor/final-audit/`.

Fresh source/static/domain regression: **24/24 commands exited 0**. Java-25 Maven/Testcontainers/Docker execution is not claimed because the current host is Java 21, cannot resolve Maven Central for Wrapper download, and has no Docker command.
