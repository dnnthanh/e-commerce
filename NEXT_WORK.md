# Next Work Handoff

> Purpose: use this file as the starting context for the next chat/session. Do not redo the completed Keycloak user-context work unless a regression is found.

## Current baseline

- Target integration branch: `develop`.
- Keycloak user-context/authentication foundation is implemented on the current feature branch.
- PR CI is intentionally MSA-oriented. The previous all-in-one `full-stack-smoke` job was removed because it forced all services, heavy databases, and observability components onto one GitHub-hosted runner and caused severe disk/resource pressure.
- Current required CI coverage is:
  - static verification;
  - backend Maven `clean verify`;
  - storefront/admin frontend builds;
  - Docker image builds;
  - Compose configuration validation, including `full`, `heavy`, and `observability` profiles;
  - core infrastructure runtime smoke for PostgreSQL, Redis, Kafka, Keycloak, and OpenSearch.

## Local development startup

`docker-build` is a GitHub Actions job. It only proves that images can be built; it does **not** start the local marketplace.

The canonical local entrypoint is the existing root script `compose-up.sh`.

### Normal local stack

```bash
./compose-up.sh up -d --build
```

### Full application profile

```bash
./compose-up.sh --profile full up -d --build
```

### Full application + heavy databases

Use this when Oracle and SQL Server are required locally:

```bash
./compose-up.sh --profile full --profile heavy up -d --build
```

The `heavy` profile includes the heavy database/data paths, including Oracle and SQL Server seed jobs.

### Optional observability

```bash
./compose-up.sh --profile observability up -d
```

Running every profile together is possible but should not be the normal developer path because it is resource-heavy:

```bash
./compose-up.sh --profile full --profile heavy --profile observability up -d --build
```

### Status and shutdown

```bash
./compose-up.sh ps
./compose-up.sh down
```

When profile-owned resources were started, use the same profiles for cleanup when needed, for example:

```bash
./compose-up.sh --profile full --profile heavy down --remove-orphans -v
```

### Database initialization, migrations, and seed order

The local stack already has the required mechanisms; do not add another wrapper shell unless `compose-up.sh` stops meeting the need.

Expected startup flow:

1. Compose starts infrastructure/databases.
2. PostgreSQL bootstrap creates the logical service databases.
3. Relational backend services execute their service-owned Liquibase changelogs on startup.
4. Demo seed containers populate deterministic development data.
5. `heavy` additionally starts heavy database paths such as Oracle/SQL Server and their corresponding seed jobs.

Relational schema evolution remains owned by Liquibase. Seed data is separate from schema migration.

## Highest priority: frontend/UI completion

The frontend currently proves integration concepts but is far too thin for a realistic marketplace. The next frontend feature should turn the current large shells into actual routed user flows and reusable UI components.

Use `frontend/COVERAGE-MATRIX.md` as a source of truth and update it as pages become real.

### Storefront gaps

Prioritize real routes/pages and end-to-end navigation for:

- application shell, router, navigation, responsive layout;
- catalog/category listing;
- product detail;
- search results and filters;
- cart page;
- checkout flow;
- login/auth callback UX and protected-route guards;
- account/profile;
- order history;
- order detail/tracking;
- consistent loading, empty, error, forbidden, and unauthorized states;
- shared product/card/price/promotion/cart components;
- breaking the current large `AppComponent` into routed feature areas.

### Admin gaps

Existing order/inventory coverage is only a starting point. Add real admin routes/pages for:

- product/catalog management;
- campaign/promotion management;
- seller management;
- reconciliation/support tooling;
- operations/fulfillment/delivery views;
- notification/preferences administration where applicable;
- review moderation;
- authorization-aware navigation/actions;
- breaking the current admin shell into routed feature modules/components.

### Frontend verification expectation

- Keep storefront and admin production builds green.
- Add smoke/E2E coverage for important real UI flows as pages are introduced.
- Capture screenshots/evidence of actual running states for completed UI features.

## Known backend runtime wiring follow-ups

The removed all-in-one full-stack smoke exposed real application startup issues. They are not considered fixed merely because that heavyweight CI job was removed.

Investigate and fix architecture-consistently:

1. `be-pricing-api`: missing Spring bean for `EffectivePriceResolver`.
2. `be-promotion-api`: missing Spring bean for `PromotionEngine`.
3. `be-search-api`: reactive application startup reports no `ReactiveWebServerFactory`; verify starter/dependency/application-type wiring.
4. `be-payment-simulator`: missing `KafkaTemplate<String, Object>` bean; verify platform Kafka auto-configuration and module dependencies.

Preserve Hexagonal/DDD boundaries. Do not solve missing wiring by scattering generic `@Component` annotations onto domain classes.

## Compose and observability follow-ups

- Replace the Kafka healthcheck that repeatedly launches the Kafka JVM CLI with a lightweight readiness/TCP-style check where appropriate.
- Fix/verify Oracle Database Observability Exporter `2.4.2` configuration schema. The exporter expects the current config structure (including plural `listenAddresses` and database entries in the expected map shape).
- Re-test OpenSearch/Dashboards under realistic disk limits; the old single-runner full stack reached very high disk usage and triggered OpenSearch disk-watermark behavior.
- Do not reintroduce the old all-in-one PR `full-stack-smoke` job.

## CI next evolution

If deeper runtime coverage is needed, prefer independent MSA-sized jobs/runners instead of one giant stack. Candidate slices:

- Oracle integration smoke;
- SQL Server integration smoke;
- pricing startup/integration smoke;
- promotion startup/integration smoke;
- search/OpenSearch smoke;
- payment/Kafka smoke;
- observability smoke.

Each slice should start only the service and dependencies needed for that scenario. A true all-services full-stack run, if retained at all, should be manual/nightly or run on suitably sized self-hosted infrastructure rather than gate every PR on one small hosted runner.

## Suggested order for the next chat

1. Build the missing storefront routed pages and shared UI foundation.
2. Expand admin routes/pages.
3. Fix the known backend runtime wiring failures discovered by the previous full-stack smoke.
4. Add targeted Oracle/SQL Server/service-level runtime smoke jobs where they add real value.
5. Harden Kafka/observability health checks and exporter configuration.

## Guardrails

- Read `AGENTS.MD`, `.agent/PLAN.MD`, `.agent/CONVENTIONS.MD`, and the relevant feature spec before implementation.
- Do not implement unrelated business domains early just to make demos appear fuller.
- Keep migrations service-owned and Liquibase-managed.
- Keep heavy integration checks isolated rather than coupling every microservice into one CI runner.
