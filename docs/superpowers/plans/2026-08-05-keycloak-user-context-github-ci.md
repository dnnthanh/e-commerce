# Keycloak User Context and GitHub CI Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make Keycloak the single authorization authority, expose complete user/profile/permission/shop-context APIs, remove seller-DB permission ownership, fix the current OpenSearch Compose blocker, and add GitHub Actions quality gates.

**Architecture:** `be-authorization-api` queries Keycloak Admin API through its outbound port and maps canonical Keycloak group paths into seller/shop scopes. Seller persistence retains business lifecycle/shop data only. Docker keeps one authorization container; CI exercises Java/Angular/static/Docker build gates while heavy databases are profile/config validated.

**Tech Stack:** Java 25, Spring Boot 4.1, Keycloak Admin REST, MapStruct, Liquibase, Docker Compose, Angular 22, GitHub Actions.

## Global Constraints

- Keycloak is the only authentication/authorization source of truth.
- Do not put the complete permission set into the JWT contract.
- Role -> permission composition remains in Keycloak.
- Seller/shop scopes are Keycloak group-tree memberships.
- Relational authorization tables may store audit/reconciliation intent only, never effective access decisions.
- API controllers remain interface-driven and use `UserContext`, not `@AuthenticationPrincipal`.
- Java target remains 25; GitHub Actions is the authoritative compile/runtime gate when local sandbox lacks Java 25/Docker.

---

### Task 1: Keycloak scope/context model

**Files:**
- Create application DTOs under `backend/services/be-authorization-api/src/main/java/.../application/dto/`.
- Modify `AuthorizationProviderPort`, `AuthorizationUseCase`, `AuthorizationServiceImplement`.
- Test `KeycloakGroupScopeResolver` and use-case projections.

**Interfaces:** provider produces profile + effective roles/permissions + canonical group paths; application derives compatibility snapshot, permission view, managed-shop view and full context.

- [ ] Write failing scope-resolver tests for seller-parent and selected-shop memberships.
- [ ] Verify the resolver tests fail before the resolver exists.
- [ ] Implement the pure scope resolver and context DTOs.
- [ ] Add use-case query methods and verify static/unit contract.

### Task 2: Keycloak REST adapter and HTTP APIs

**Files:**
- Modify `KeycloakAuthorizationRestAdapter`.
- Modify `AuthorizationApi`, `AuthorizationController`, `AuthorizationApiMapper`.
- Create top-level response/request records for profile, permissions, managed shops, full context and shop scope mutation.

**Interfaces:** resolve user representation, composite realm roles and user groups from Keycloak; resolve group by canonical path for scope assignment; never mutate parent `subGroups` payloads.

- [ ] Add failing contract tests/static checks for the new endpoint paths and group-path semantics.
- [ ] Implement Keycloak user/profile/context reads and shop scope mutation.
- [ ] Implement current/internal endpoint projections.
- [ ] Verify no transport DTO leaks into application/outbound packages.

### Task 3: Remove seller DB authorization

**Files:**
- Modify `SellerAccount`, `SellerPersistenceAdapter` and tests.
- Delete `SellerStaffJpaEntity` and `SellerStaffJpaRepository`.
- Add `003-drop-seller-staff-authorization.sql` and include it in Liquibase master changelog.

**Interfaces:** seller aggregate exposes lifecycle only; authorization is delegated to platform authorization service/Keycloak.

- [ ] Replace the old staff-permission domain test with lifecycle-only assertions.
- [ ] Remove staff permission state and persistence code.
- [ ] Add schema migration dropping `seller_staff`.
- [ ] Run source scan proving seller service no longer contains permission-storage/evaluation code.

### Task 4: Keycloak bootstrap tree and Docker fixes

**Files:**
- Modify `infrastructure/keycloak/realm-marketplace.json`.
- Modify `docker-compose.yml` and relevant Compose fragments.

**Interfaces:** seed seller groups contain `/shops/shop-*` children with type/sellerId/shopId attributes; OpenSearch dev starts with demo security installation disabled.

- [ ] Add static failing verification for expected group tree and OpenSearch env.
- [ ] Update realm seed and Compose.
- [ ] Verify `be-authorization-api` is in default topology and heavy profiles remain optional.

### Task 5: CI and persistent project context

**Files:**
- Create `.github/workflows/ci.yml`.
- Create `docs/PROJECT-CONTEXT.md`.
- Create/update verification script/report under `verification/` and `.agent/reports/`.

**Interfaces:** GitHub Actions runs Java 25 backend tests/package, Angular builds, verification scripts, Compose config and Docker build gates; context file records architecture/history needed by future sessions.

- [ ] Add CI workflow syntax/static verification.
- [ ] Add project context summary with current branch/repo/runtime decisions.
- [ ] Run all available static gates locally.
- [ ] Push feature branch to GitHub, open PR to `develop`, inspect Actions status/logs and fix any failures before reporting completion.
