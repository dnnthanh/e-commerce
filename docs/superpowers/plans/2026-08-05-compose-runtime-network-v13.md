# Docker Compose Runtime Network v13 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make the full local marketplace stack boot with consistent Compose DNS, internal port 8080 for Spring services, and complete runtime environment variables.

**Architecture:** Docker Compose service names are the stable DNS contract for container-to-container traffic. Host published ports are only for developer/browser access. Spring services listen on container port 8080; infrastructure keeps its native internal ports. Keycloak's public issuer remains `http://localhost:8180/realms/marketplace` because tokens are issued for the browser-visible hostname, while JWK/token/admin backchannel traffic uses `keycloak:8080`.

**Tech Stack:** Docker Compose, Spring Boot 3/JDK 25, PostgreSQL, MySQL, SQL Server, Oracle, MongoDB, Redis, Kafka, Keycloak, MinIO, OpenSearch.

## Global Constraints

- Container-to-container URLs MUST use Compose service names, not `localhost`.
- Spring HTTP services MUST listen on internal port `8080`.
- Host ports MUST map `<developer-port>:8080` for Spring API/gateway/realtime/simulator services.
- Health checks are allowed to use `localhost` because they run inside the target container, but Spring health checks MUST use port `8080`.
- Keycloak issuer is a public identity value and remains browser-visible `http://localhost:8180/realms/marketplace`; actual backchannel HTTP uses `keycloak:8080`.
- Public media URLs returned to browsers may use `localhost:9000`; MinIO SDK traffic MUST use `minio:9000`.
- Local-built `e-commerce-*` images MUST use `pull_policy: build`.
- Required `${ENV}` placeholders in each `application.yml` MUST be supplied by local Compose.

---

### Task 1: Add runtime topology regression gate
- [ ] Verify every required application placeholder is provided by Compose.
- [ ] Verify internal `http://be-*` URLs target port `8080`.
- [ ] Verify published Spring ports map to container `8080` and health checks use `localhost:8080`.
- [ ] Verify local-built images use `pull_policy: build`.

### Task 2: Normalize service runtime environment
- [ ] Add the correct DB/Mongo URL and credentials for each service family.
- [ ] Add all required bounded-context base URLs using Compose DNS.
- [ ] Normalize authorization/payment/search/media internal URLs.
- [ ] Add Keycloak admin variables required by authorization.

### Task 3: Normalize root and split compose files
- [ ] Apply the same runtime contract to `docker-compose.yml` and `compose/**/*.yml`.
- [ ] Preserve host-facing developer ports.
- [ ] Keep infrastructure native ports unchanged.

### Task 4: Update local architecture documentation
- [ ] Document service-name DNS vs published host ports.
- [ ] Document the Keycloak issuer/backchannel exception and media public/internal URL split.

### Task 5: Verify and package
- [ ] Run YAML parse and v13 runtime gate.
- [ ] Run relevant existing Compose/static gates.
- [ ] Scan for stale internal `localhost` and wrong internal service ports.
- [ ] Package v13 and verify ZIP integrity.
