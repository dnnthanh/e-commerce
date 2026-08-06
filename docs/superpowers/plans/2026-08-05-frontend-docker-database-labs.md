# Frontend, Docker Topology, and Production Database Labs Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [x]`) syntax for tracking.

**Goal:** Align Angular with the backend API envelope/pagination contracts, provide maintainable per-surface Docker/Compose entrypoints while retaining one full-stack command, and turn database-labs into production troubleshooting exercises rather than syntax demos.

**Architecture:** The frontend owns typed API envelopes and unwraps normal/page/cursor responses centrally. Docker uses one generic backend service Dockerfile plus dedicated Angular Dockerfiles and compose fragments; the root compose remains the canonical full-stack entrypoint. Database labs are scenario-first: realistic skewed data, baseline plan, diagnosis, alternative fixes, trade-offs, verification, rollback/regression criteria.

**Tech Stack:** Angular 22/TypeScript, Docker/Compose, Java 25/Spring Boot multi-module Maven, PostgreSQL/Oracle/SQL Server/MySQL.

## Global Constraints

- No NiFi assets, scenarios, or assumptions.
- `docker compose up -d --build` at repository root remains the canonical full-stack command.
- Do not duplicate one backend Dockerfile per service; parameterize the module/artifact.
- Frontend error handling must preserve backend `error.code`, localized `message`, and `traceId`.
- Relational pageable APIs use `page/size`; search/discovery cursor behavior stays cursor/search-after where exposed.
- Database labs must include production diagnosis and trade-offs, not only happy-path SQL snippets.

---

### Task 1: Frontend API Contract
- [x] Add typed normal/page/cursor/error API envelope contracts.
- [x] Refactor `ApiService` to unwrap data centrally, return page metadata for pageable calls, preserve trace-id, and normalize backend errors.
- [x] Update direct auth/notification fetch paths to understand the backend envelope where applicable.
- [x] Migrate pageable admin/storefront calls from `limit`/array assumptions to `page/size` and typed page results.
- [x] Build both Angular projects.

### Task 2: Docker and Compose Topology
- [x] Normalize invalid/IDE-hostile healthcheck list syntax in the root compose.
- [x] Add one generic backend `Dockerfile.service` using a `MODULE` build argument.
- [x] Add dedicated storefront/admin Dockerfiles and shared nginx runtime configuration.
- [x] Add infrastructure, frontend, and per-backend-service compose fragments while retaining root `docker-compose.yml` as the all-services entrypoint.
- [x] Add compose validation scripts/docs and validate all YAML/Compose files available in the environment.

### Task 3: Production Database Labs
- [x] Rewrite the database-labs guide around a repeatable troubleshooting workflow.
- [x] Add realistic PostgreSQL cases for CTE/join explosion, wide-row/TOAST reads, offset instability/keyset pagination, bad cardinality estimates/extended stats, partial-covering indexes, work_mem spill, outbox SKIP LOCKED, lock/deadlock/advisory locking, connection-pool pressure, JSONB/GIN, GiST overlap, BRIN/time-series, partition pruning/pg_partman, vacuum/bloat, and index write amplification.
- [x] Strengthen Oracle, SQL Server, and MySQL labs with engine-specific production cases and plan-reading checklists.
- [x] Ensure each advanced case states data shape, baseline, evidence to capture, hypotheses, candidate fixes, trade-offs, and regression criteria.
- [x] Remove any NiFi references.

### Task 4: Verification
- [x] Run TypeScript/Angular builds where dependencies are available.
- [x] Parse every YAML file and validate root/fragment Compose syntax with available Docker tooling.
- [x] Run repository static/architecture gates relevant to modified files.
- [x] Scan repository for prohibited NiFi references in database labs.
- [x] Package the verified repository snapshot.
