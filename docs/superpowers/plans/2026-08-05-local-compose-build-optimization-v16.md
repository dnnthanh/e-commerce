# Local Compose Build Optimization v16 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [x]`) syntax for tracking.

**Goal:** Replace 51 independent local Maven image builds with one reactor-built shared backend runtime and make the default Compose topology fit a developer workstation.

**Architecture:** `backend-runtime-build` is the sole local backend image build owner. `backend/Dockerfile.runtime` packages the full Maven reactor once and stores each service JAR in a shared Java 25 runtime image; backend containers select their JAR via `SERVICE_MODULE`. Compose profiles keep heavy, asynchronous, and observability workloads opt-in.

**Tech Stack:** Docker Compose, BuildKit, Maven 3.9/JDK 25, Spring Boot, Angular 22, TypeScript 6, Python static verification.

## Global Constraints

- Keep service-to-service DNS on Compose service names and internal port 8080.
- Keep host published ports unchanged.
- Do not use `container_name` as an application endpoint.
- Keep per-service Dockerfile support for CI/production.
- Preserve existing `seed-large` opt-in behavior.
- Local default must not start Oracle, settlement, asynchronous workers/outboxes/jobs, or observability tooling.

---

### Task 1: Add failing optimization gate

**Files:**
- Create: `verification/verify_local_compose_optimization_v16.py`

**Interfaces:**
- Consumes: root Compose, backend Dockerfiles, frontend package/workspace files.
- Produces: deterministic PASS/FAIL contract for v16.

- [x] Write the verifier to reject per-service backend `build:` blocks, missing shared runtime, missing `SERVICE_MODULE`, missing profiles, and Angular/TypeScript mismatch.
- [x] Run it against v15 and confirm failure.

### Task 2: Implement shared backend runtime

**Files:**
- Create: `backend/Dockerfile.runtime`
- Create: `backend/docker/runtime-entrypoint.sh`
- Modify: `docker-compose.yml`
- Modify: `compose/backend/all.yml`
- Modify: `compose/backend/be-*.yml`

**Interfaces:**
- Consumes: Maven reactor service modules.
- Produces: `e-commerce-backend-runtime:local` with `/opt/marketplace/services/<module>/app.jar`; `SERVICE_MODULE` selects the executable.

- [x] Build the Maven reactor once with a shared BuildKit Maven cache and bounded Maven heap.
- [x] Collect executable service JARs and fail image build if any `be-*` service lacks one.
- [x] Convert all local backend Compose services to the shared image and inject `SERVICE_MODULE`.
- [x] Keep host ports, healthchecks, env, networks, and service dependencies intact.

### Task 3: Add local runtime profiles and memory bounds

**Files:**
- Modify: `docker-compose.yml`
- Modify: `compose/infrastructure.yml`
- Modify: `compose/seeds.yml`
- Modify: `compose/frontend.yml`
- Modify: `.env`
- Modify: `compose-up.sh`

**Interfaces:**
- Produces: default dev stack, plus `full`, `heavy`, `observability`, and existing `seed-large` opt-ins.

- [x] Profile Oracle/settlement under `heavy` and `full`.
- [x] Profile Mongo/comment/notification and async jobs/outbox/workers under `full`.
- [x] Profile dashboards/exporters/LGTM/Alloy/CloudBeaver under `observability` and `full`.
- [x] Apply bounded local JVM heap defaults and keep Compose parallelism conservative.
- [x] Remove host interpolation of container-only MSSQL password variables.

### Task 4: Fix Angular 22 production builds

**Files:**
- Modify: `frontend/package.json`
- Modify: `frontend/angular.json`

**Interfaces:**
- Produces: Angular 22 / TypeScript 6-compatible dependency tree and explicit production configuration for both applications.

- [x] Upgrade TypeScript to `~6.0.0`.
- [x] Define production/development build configurations and production defaults for storefront/admin.
- [x] Verify JSON and workspace structure statically.

### Task 5: Documentation and regression verification

**Files:**
- Modify: `compose/README.md`
- Modify: `README.md`
- Modify: `docs/architecture/014-internal-communication-and-cicd-config.md`

**Interfaces:**
- Produces: developer commands for default, full, heavy, and observability workflows.

- [x] Document `docker compose build backend-runtime-build` and normal `docker compose up -d --build` behavior.
- [x] Document profile commands and resource rationale.
- [x] Run v16 gate and all compatible previous static gates.
- [x] Create ZIP and run archive integrity verification.
