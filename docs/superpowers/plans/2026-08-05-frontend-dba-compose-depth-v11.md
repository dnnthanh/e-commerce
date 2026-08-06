# Frontend, DBA Labs, and Compose Depth V11 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make the marketplace runnable with Docker Compose on macOS, expand the Angular apps to cover the core marketplace/operator workflows exposed by backend v10, and replace shallow database examples with deep production DBA incident labs across PostgreSQL, MySQL, SQL Server, and Oracle.

**Architecture:** Keep one root `docker-compose.yml` for full-stack startup, but scope backend and frontend build contexts to their own directories so Docker never needs a parent (`..`) build context. Frontend uses the shared API envelope client plus a typed marketplace facade, with storefront and admin features mapped to bounded-context APIs. Database labs use engine-specific shared million-row bootstrap datasets and incident packs containing baseline, diagnosis, candidate fixes, regression, and operator checklists.

**Tech Stack:** Docker Compose, multi-stage Dockerfiles, Angular 22, TypeScript 5.8, Java/Spring backend API contracts, PostgreSQL 17, MySQL 8.4, SQL Server 2022, Oracle Free 23.

## Global Constraints

- No NiFi references or labs.
- Root `docker compose up -d --build` remains the full-stack entrypoint.
- No compose build context may escape the repository or rely on `..` when invoked from the root compose file.
- FE must consume `ApiResponse<T>` envelopes, page metadata, cursor metadata, enum JSON codes, and backend trace IDs.
- Database labs must be production-incident style, not syntax demos.
- Every database engine gets deep labs, not PostgreSQL-only depth.
- Update architecture/spec/preview/readme documents when runtime or product scope changes.

---

### Task 1: Compose Runtime and Build Contexts

**Files:**
- Modify: `docker-compose.yml`
- Create: `backend/Dockerfile.service`
- Create: `backend/docker/service-entrypoint.sh`
- Create: `frontend/Dockerfile.storefront`
- Create: `frontend/Dockerfile.admin`
- Modify: `compose/backend/*.yml`
- Modify: `compose/frontend.yml`
- Modify: `compose/README.md`
- Test: `ci-cdconfigs/verify_compose_runtime_paths_v11.py`

**Interfaces:**
- Produces root-safe Docker build contexts used by all compose entrypoints.

- [x] Write a failing verifier that rejects parent/ambiguous build contexts and missing Dockerfiles.
- [x] Run the verifier and confirm v10 fails on `compose/backend/all.yml` and old broad build contexts.
- [x] Move generic backend and frontend Dockerfiles into their scoped contexts and rewrite compose build definitions.
- [x] Validate every compose YAML file and resolved Dockerfile path.
- [x] Document macOS Downloads/File Sharing troubleshooting separately from compose path errors.

### Task 2: Frontend API Coverage and Business Flows

**Files:**
- Create: `frontend/shared/marketplace-types.ts`
- Create: `frontend/shared/marketplace-api.service.ts`
- Modify: `frontend/projects/storefront/src/app/app.component.ts`
- Modify/Create: storefront feature components for product/cart/checkout/order/returns/review/comment/notification/seller/account.
- Modify: `frontend/projects/admin/src/app/app.component.ts`
- Modify/Create: admin feature components for catalog/media/pricing/promotion/inventory/order/fulfillment/payment/return/moderation/seller/settlement/security/audit/operations.
- Modify: frontend styles and README.
- Test: `ci-cdconfigs/verify_frontend_backend_coverage_v11.py`

**Interfaces:**
- Consumes backend v10 private/public endpoint contracts.
- Produces a typed frontend facade and route-level workflows.

- [x] Write a failing coverage verifier for required bounded-context routes/endpoints and minimum non-demo feature depth.
- [x] Add typed response/request models and facade methods.
- [x] Expand storefront workflows so checkout is cart-driven and order/return/product detail expose cross-context data.
- [x] Expand admin workflows to include all major operational bounded contexts.
- [x] Run TypeScript syntax/static checks and the coverage verifier.

### Task 3: Deep DBA Labs for All Engines

**Files:**
- Create/Modify: `database-labs/postgresql/08-deep-incidents/**`
- Create/Modify: `database-labs/mysql/08-deep-incidents/**`
- Create/Modify: `database-labs/sqlserver/08-deep-incidents/**`
- Create/Modify: `database-labs/oracle/08-deep-incidents/**`
- Modify: `database-labs/README.md`, `LEARNING-GUIDE.md`, `INCIDENT-TEMPLATE.md`
- Test: `ci-cdconfigs/verify_database_lab_depth_v11.py`

**Interfaces:**
- Each incident produces `README.md`, `baseline.sql`, `solutions.sql`, and `regression.sql`, backed by an engine bootstrap dataset.

- [x] Write a failing depth verifier requiring multiple deep incident packs per engine and mandatory diagnosis/trade-off/regression sections.
- [x] Add engine bootstraps with realistic skew/volume patterns.
- [x] Build deep incident packs covering plan estimation, memory/spills, locking/concurrency, indexing, partitioning, pagination, MVCC/storage maintenance, and plan regression per engine.
- [x] Add operator evidence commands and acceptance criteria to each incident.
- [x] Confirm no NiFi references exist anywhere in database labs.

### Task 4: Architecture and Product Documentation

**Files:**
- Modify: `docs/superpowers/specs/2026-08-02-marketplace-platform-design.md`
- Create: `docs/architecture/015-frontend-runtime-and-dba-lab-depth.md`
- Modify: `docs/preview/PROJECT_PREVIEW.md`
- Modify: root `README.md`
- Modify: `frontend/README.md`, `database-labs/README.md`, `compose/README.md`

**Interfaces:**
- Documents the same runtime/build/FE/DBA architecture implemented by Tasks 1-3.

- [x] Update system boundaries and developer workflows.
- [x] Document full-stack compose and service-specific compose commands.
- [x] Document FE coverage matrix against backend bounded contexts.
- [x] Document DBA lab maturity levels and expected evidence.

### Task 5: Final Verification and Packaging

**Files:**
- Modify: `ci-cdconfigs/run_feature013_final_audit.sh` to include v11 gates.

**Interfaces:**
- Produces a single auditable v11 snapshot.

- [x] Run v11 compose, FE, and DBA gates.
- [x] Run existing source/architecture gates that do not require unavailable external runtimes.
- [x] Validate all YAML and ZIP integrity.
- [x] Package `e-commerce-4-fixed-v11.zip`.


## Verification status

- Static Compose path/YAML verification: PASS.
- Frontend bounded-context coverage and TypeScript parse verification: PASS.
- Deep DBA incident verification for PostgreSQL/MySQL/SQL Server/Oracle: PASS.
- Angular production build could not run in the artifact sandbox because its npm mirror does not contain Angular 22 packages; run the documented build on a normal npm registry.
- Docker runtime could not run in the artifact sandbox because the Docker CLI/daemon is unavailable; `compose-up.sh config` and `compose-up.sh up -d --build` are the local runtime gates.
