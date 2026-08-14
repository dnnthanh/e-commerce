# Feature 015 — Admin Console Core Flow Verification Report

## Scope

Approved specification: `.agent/specs/015-admin-console-core-flow.md`

Implementation plan: `docs/superpowers/plans/2026-08-11-admin-console-core-flow.md`

Draft PR: https://github.com/dnnthanh/e-commerce/pull/5

This report records observed execution evidence. It does not claim a command or screenshot that has not been produced by GitHub Actions.

## TDD evidence

### RED

- Commit: `f65e5cffedea5d3cab98c34aa25bba4bed335445`
- GitHub Actions run: `31471921896`
- `backend`: failed because `CommentApiContractTest` required browser-safe `/private/comments/{threadId}/hide|unhide` mappings that did not yet exist.
- `static-verification`: failed because the Feature 015 Admin shell/navigation/typed moderation/Playwright contracts did not yet exist.
- No production implementation had been added at this checkpoint.

### GREEN progression

- The existing Comment hide/unhide use cases were exposed through additional `/private/**` aliases while retaining the existing `/internal/**` mappings and `COMMENT_MODERATE` authorization.
- The new backend contract test passed after the alias change. A later Maven failure was Spotless-only on the touched Java files; formatting was corrected without changing behavior.
- CI run `31473765124` subsequently showed backend, frontend, Compose validation, Docker build, and core infrastructure smoke passing; static verification remained intentionally incomplete until real Admin evidence was added.
- Current pre-squash SHA `5ce0f8fc2f757be66707d60d8ce88719b94c7398` has Feature 015 static verification passing in both push run `31475656885` and PR run `31475744283`.

## Real-service Admin evidence

Workflow: `.github/workflows/admin-visual.yml`

Initial PR-triggered run: `31475744374`

The workflow uses real services only:

- Keycloak `admin-console` client and demo identities;
- PostgreSQL Catalog/Operations/Authorization data;
- MySQL Review data;
- MongoDB Comment data;
- SQL Server Order/Fulfillment data;
- Redis and Kafka infrastructure;
- real `be-authorization-api`, `be-catalog-api`, `be-review-api`, `be-comment-api`, `be-order-api`, `be-fulfillment-api`, `be-operations-api`, and `be-gateway` containers;
- Angular Admin served on port 4201;
- no Playwright `page.route()` business mocking and no injected bearer-token shortcut.

Expected artifact paths, populated only by a successful Playwright run:

- `frontend/playwright-artifacts/admin-core-flow/catalog-real-action-desktop.png`
- `frontend/playwright-artifacts/admin-core-flow/forbidden-seller-desktop.png`
- `frontend/playwright-artifacts/admin-core-flow/moderation-hidden-comment-desktop.png`
- `frontend/playwright-artifacts/admin-core-flow/order-to-fulfillment-desktop.png`
- `frontend/playwright-artifacts/admin-core-flow/mobile-admin-navigation.png`

Real flow assertions:

1. seller operator sees permission-aware navigation and cannot see Security/Moderation;
2. seller operator creates, searches, confirms and publishes a real Catalog draft;
3. authenticated seller direct-navigation to Security produces explicit `/forbidden` state;
4. platform admin hides a real seeded Comment through the private moderation command, captures HIDDEN, then restores ACTIVE;
5. a seeded Order drill-down carries its order number into Fulfillment;
6. mobile Admin navigation opens at 390×844 without horizontal overflow.

Status at report creation: **workflow running; do not treat this section as PASS until the run concludes successfully and artifacts are listed.**

## Required final quality gate

Before handoff the final one-commit SHA must have all of the following observed green again:

- `static-verification`
- backend Maven `clean verify`
- Storefront and Admin Angular production builds
- default and full/heavy/observability Compose config validation
- Docker runtime/Admin image builds and media seed verification
- core infrastructure smoke
- Admin real-data visual Playwright workflow
- Storefront real-data visual workflow when triggered by the PR path set

The branch is not merge-ready until it is rewritten to exactly one logical commit on top of the latest `develop`, the PR points at that rewritten SHA, and all required checks are green for that exact SHA.
