# Marketplace Project Context

Last updated: 2026-08-05.

This file is the durable hand-off context for future ChatGPT/Codex sessions. Read `AGENTS.MD`, `.agent/PLAN.MD`, `.agent/CONVENTIONS.MD`, the relevant `.agent/specs/*` file, and this document before continuing implementation.

## Repository and branch policy

- GitHub repository: `dnnthanh/e-commerce` (private).
- `master` is production-ready only.
- `develop` is integration.
- Changes use `feature/*`, `fix/*`, or `chore/*` branches and merge through PRs.
- The repository originally contained only a placeholder README/.gitignore; the current marketplace snapshot is being promoted into GitHub as the canonical source.

## Product/architecture

Production-oriented multi-seller marketplace using Java 25 / Spring Boot 4.1, Angular 22, Kafka and polyglot persistence.

Main bounded contexts include platform foundation, authorization, gateway, seller/shop, catalog, media, search/discovery, pricing, promotion, inventory/reservation, cart, checkout/order, payment/refund, fulfillment, review/comment, notification/realtime, return, settlement, operations and audit.

Internal synchronous calls currently use REST. Kafka is used for asynchronous domain/integration events and outbox flows. gRPC is intentionally not introduced without a concrete high-volume/streaming need.

## Authentication and authorization — authoritative decision

Keycloak is the **only source of authentication and authorization truth**.

- Authentication: OIDC/Keycloak.
- Roles aggregate permissions using Keycloak composite realm roles.
- Backend business decisions are permission-based (`@PreAuthorize` + `authorizationService`).
- JWT is intentionally small and treated as identity, not as the complete permission store.
- Human clients should not depend on composite permission expansion in the JWT.
- Effective roles, permissions and resource scopes are resolved through `be-authorization-api`, backed by Keycloak Admin API and short-lived cache.
- Internal HTTP calls use Keycloak client credentials/service identity.
- Authorization persistence in `be-authorization-api` is allowed only for durable change intent, reconciliation and audit/outbox; it is never queried as the access authority.
- `be-seller-api` must not persist/evaluate staff permissions. Seller DB owns business seller/shop data only.

### Keycloak group tree

Canonical tree:

```text
/sellers
  /seller-{sellerId}
    /shops
      /shop-{shopId}
```

Group attributes:

- seller: `type=SELLER`, `sellerId`.
- shops container: `type=SHOP_CONTAINER`, `sellerId`.
- shop: `type=SHOP`, `sellerId`, `shopId`.

Membership semantics:

- seller parent membership => `ALL_SHOPS` for the seller.
- shop leaf memberships => `SELECTED_SHOPS` only.
- seller parent overrides selected leaves while present.
- group tree writes must resolve canonical path/id and use Keycloak child-group APIs; do not PUT a parent `subGroups` representation and assume nested children are persisted.

### Authorization APIs

Existing compatibility endpoint:

```text
GET /private/me/authorization
```

Additional APIs:

```text
GET /private/me/profile
GET /private/me/permissions
GET /private/me/managed-shops
GET /private/me/context
```

Equivalent service-account lookup endpoints exist under `/internal/authorization/users/{userId}/...`. Admin security context lookup uses `/private/admin/security/users/{userId}/context`.

Seller-wide and shop-specific scope mutation APIs are under `/private/admin/security/users/{userId}/seller-scopes` and `/shop-scopes` and require `SECURITY_MANAGE`.

These are endpoints inside the single `be-authorization-api` container; they are **not separate Docker containers**.

## Keycloak bootstrap

- `marketplace-internal` service account receives realm role `SERVICE_ACCOUNT` for internal endpoint authentication.
- `authorization-admin` service account receives only the realm-management capabilities required to query/mutate user roles/groups/profile (`manage-users`, `view-users`, `query-users`, `query-groups`, `view-realm`).
- Demo realm contains seller/shop scope groups matching MySQL demo seller/shop ids.

## Docker/local-runtime decisions

Earlier local Compose attempted to build ~51 backend images independently and could kill Docker Desktop even on an Apple M4 Pro / 24 GB machine. The optimized model is:

```text
one Maven reactor build -> e-commerce-backend-runtime:local -> many containers select SERVICE_MODULE
```

Local runtime uses bounded JVM heaps and Compose profiles to avoid starting every heavy service for normal development.

Profiles:

- default: practical dev marketplace flow.
- `full`: full bounded-context topology.
- `heavy`: Oracle/other resource-heavy components.
- `observability`: dashboards/exporters/tooling.
- `seed-large`: large deterministic datasets.

Container-to-container HTTP uses Compose service DNS + container port (normally `http://be-xxx:8080`). Host/browser access uses published `localhost:<hostPort>`. Do not use `localhost` for service-to-service calls.

Local-built images use explicit build/pull behavior; public registry images remain pullable.

### OpenSearch

OpenSearch 3.7 dev mode must explicitly disable Docker demo security bootstrap:

```text
DISABLE_INSTALL_DEMO_CONFIG=true
DISABLE_SECURITY_PLUGIN=true
```

and use `nofile` ulimit 65536. Merely setting `plugins.security.disabled=true` was insufficient for the Docker startup path and caused `opensearch` to exit, blocking dependent services.

### SQL Server on Apple Silicon

Microsoft SQL Server image may run as `linux/amd64` under emulation on ARM64 and emits a platform warning. It belongs in heavier/full workflows rather than the lightest local path where practical.

### Oracle exporter

Do not compile `godror` with `CGO_ENABLED=0`. Local Compose uses Oracle's prebuilt multi-arch observability exporter image instead of compiling it from source.

### Kafka JMX exporter

Standalone JMX exporter is downloaded from the official release asset, not a stale Maven Central URL that returned 404.

## Frontend decisions/fixes

- Angular 22.
- TypeScript `~6.0.0` to satisfy Angular 22 peer requirements.
- `production` build configurations exist for storefront and admin.
- Docker production build executes real `ng build ... --configuration production`.
- Storefront/admin share envelope/page/cursor API contracts.
- Public frontend must not call backend `/internal/**` APIs.

## API/platform conventions already established

- Generic success envelope `{data}`, page `{data, metadata}`, errors `{error}`; nulls omitted.
- Relational pagination uses Spring `Pageable/Page`; OpenSearch search uses cursor/search-after.
- MapStruct Spring mappers are preferred; mapping must not contain business rules.
- Controllers are interface-driven.
- `UserContext` is request-scoped; controllers do not use `@AuthenticationPrincipal`.
- Trace header convention is `trace-id`; OpenTelemetry/W3C tracing is used.
- Error codes are typed and localized.
- Transactional Outbox is used where DB+Kafka consistency is required.
- No cross-service database access.

## Database learning labs

The repository includes deep DBA incident labs for PostgreSQL, MySQL, SQL Server and Oracle, including plans, skew/cardinality, locks/deadlocks, pagination, partitioning, spill/memory, JSON/indexing and regression/acceptance analysis. NiFi is intentionally not part of these labs.

## CI expectations

GitHub Actions is the authoritative remote quality gate because the ChatGPT sandbox does not have Docker daemon and locally available Java may not be Java 25.

CI should check:

- Java 25 backend `clean verify`.
- Angular storefront/admin production builds.
- verification scripts.
- default/full Compose config.
- shared backend runtime + frontend/seed Docker builds.
- lightweight smoke of core infrastructure (PostgreSQL, Redis, Kafka, Keycloak, OpenSearch).
- failure log artifact upload for infrastructure startup problems.
- heavy Oracle/SQL Server runtime should not be mandatory on standard hosted runner CI.

## Current implementation focus

Feature spec: `.agent/specs/001-security-authorization.md`.
Implementation plan: `docs/superpowers/plans/2026-08-05-keycloak-user-context-github-ci.md`.

Current goals:

1. Complete Keycloak-backed profile/permissions/managed-shops/full-context APIs.
2. Keep authorization exclusively in Keycloak and remove seller DB permission ownership.
3. Keep seller/shop scope as a robust Keycloak group tree.
4. Fix OpenSearch startup blocker and keep local Docker resource usage controlled.
5. Push to GitHub on a feature branch, open PR to `develop`, and use Actions results to discover/fix remaining compile/test/Docker errors.

## Verification honesty

Do not claim Docker runtime or Java 25 compilation passed from the ChatGPT sandbox unless those commands actually ran. Use GitHub Actions and the user's Docker Desktop output as fresh runtime evidence.
