# Feature 015 — Admin Console Core Flow

## Status

Ready for user review. Visual direction approved; production implementation has not started.

## Context

Feature 014 completed the Storefront Core Flow and is merged into `develop`. `NEXT_WORK.md` identifies Admin UI completion as the next priority.

The current Angular admin application already has routes for Dashboard, Sellers, Catalog, Media, Pricing, Promotions, Inventory, Orders, Fulfillment, Payments, Returns, Moderation, Settlement, Security, Audit, and Operations, but many pages are still thin demo-style components. The current shell is also concentrated in one large inline `AppComponent`.

Feature 015 upgrades that surface into a realistic, authorization-aware marketplace operations console. It reuses existing backend domains instead of re-implementing them.

The approved visual reference is the Feature 015 preview: light main workspace, dark navy navigation rail, compact top bar, operational cards, dense tables, filters, status badges, and focused action panels.

The preview is a layout/style reference only. The repository remains Angular-based. This feature MUST NOT migrate to React, Next.js, Tailwind, Zustand, React Query, or another frontend stack.

## Goal

Deliver an Admin Console core flow that:

- has a responsive reusable shell;
- exposes only routes and actions allowed by the authenticated user's effective permissions;
- turns existing admin routes into coherent operational workflows;
- removes avoidable raw-ID-only demo interactions when selectable/searchable data is available;
- standardizes loading, empty, error, unauthorized, and forbidden states;
- uses typed frontend contracts and bounded-context facades;
- adds only the smallest browser-safe private backend contracts required by real admin workflows;
- never calls `/internal/**` from browser code;
- is verified through production builds, applicable backend tests, Playwright smoke/E2E, and screenshots from real running services.

## Design principles

### Operational console, not fake analytics

The admin application is a control plane for operators, seller administrators, support users, moderators, security administrators, finance users, and operations users.

Operational tables, filters, entity state, ownership, timestamps, actions, and traceability are more important than decorative charts.

The UI MUST NOT fabricate production-looking revenue, user, order, service-count, or trend metrics merely to resemble the preview. Dashboard cards and summaries must use real browser-safe backend data. If a useful aggregate is unavailable, show a truthful alternative or an explicit unavailable/empty state.

### Permission-aware UX; backend remains authoritative

Angular loads the effective authorization snapshot from `/private/me/authorization` after Keycloak authentication.

- Route guards provide navigation UX protection.
- Navigation items are hidden when their required permission is absent.
- Mutation/bulk-action controls are hidden or disabled when permission is absent.
- Backend `@PreAuthorize` remains authoritative.
- Frontend permission checks are never treated as the security boundary.
- Authenticated users denied access receive a dedicated forbidden state rather than a generic network error.

### Browser-safe APIs only

Angular uses public or `/private/**` contracts. Browser code MUST NOT call `/internal/**`.

When only an internal backend contract exists for a required admin action, Feature 015 may expose the same existing use case through the smallest permission-protected `/private/**` API. It must not move business ownership into the frontend or create cross-service database access.

### Reuse without building a framework

Extract repeated UI primitives where they materially reduce duplication, including page headers, stat cards, filter toolbars, state badges, loading/empty/error/forbidden states, confirmations, pagination, and entity selectors where contracts allow them.

Do not build a generic component framework for hypothetical future requirements.

## Information architecture

Recommended navigation groups:

- **Overview:** Dashboard
- **Marketplace:** Sellers, Catalog, Media, Pricing, Promotions, Inventory
- **Commerce:** Orders, Fulfillment, Payments, Returns, Settlement
- **Community:** Moderation
- **Governance:** Security, Audit, Operations

Navigation definitions and permission requirements should be centralized rather than duplicated through templates and route files.

## Scope

### 1. Admin shell and shared states

Deliver:

- dark responsive navigation rail matching the approved visual direction;
- compact top bar with environment context and authenticated identity/roles;
- grouped permission-aware navigation;
- active route state;
- desktop and narrow/mobile navigation behavior;
- separate unauthorized and forbidden UX;
- shared page/state primitives;
- smaller focused shell/routing/navigation responsibilities instead of another giant `AppComponent`.

Complex feature pages should prefer focused component/template/style files over very large inline templates.

### 2. Dashboard

Replace the current demo-style dashboard with a truthful operational landing page.

Required characteristics:

- typed data; no new `any`-based feature models;
- no hard-coded architecture/service count such as the current `53 be-* deployables` value;
- no silently converting failed widgets into successful empty data;
- real summary cards using existing bounded browser-safe contracts;
- latest incidents/operational alerts;
- useful quick links into major workflows;
- configured observability links remain supplemental.

The frontend may issue a bounded set of parallel read requests for dashboard widgets. Do not introduce a cross-domain BFF solely to generate cosmetic dashboard numbers.

### 3. Seller/shop management

Improve the seller route into a real management flow:

- seller/shop lookup or search through browser-safe contracts;
- seller/shop state summary;
- edit shop name/description where authorized;
- validation and clear save feedback;
- seller-scope authorization respected;
- avoid manual raw seller ID entry when a valid searchable/selectable contract exists.

If no browser-safe seller list/search contract exists and the workflow remains raw-ID-only, add the smallest private Seller query API needed for selection. Do not create a new seller domain model.

### 4. Catalog management

Provide a consistent product lifecycle workflow:

- product search/filter/list with deterministic pagination;
- clear seller/category/status context;
- create draft;
- detail/edit surface only where existing contracts support it;
- publish action with permission-aware confirmation;
- useful navigation to media where identifiers exist;
- reusable filter/table/state patterns.

Do not fabricate category metadata. Category may remain ID-based if the repository still lacks an appropriate browser-safe category metadata contract.

### 5. Media management

Provide:

- product media inspection/listing where existing contracts allow it;
- upload-session creation;
- upload/completion state;
- complete-upload action;
- clear product/asset relationship;
- truthful backend error/retry feedback.

### 6. Pricing and Promotions

Upgrade existing diagnostic pages into the common admin interaction model without changing pricing/promotion business semantics.

- Pricing continues to own effective price calculation.
- Promotion evaluation remains diagnostic if no private campaign CRUD contract exists.
- Do not fake campaign CRUD in the frontend.
- Do not implement new promotion rules merely to fill the screen.

### 7. Inventory

Provide:

- pageable/searchable balance view;
- SKU/warehouse filters;
- clear available/reserved/on-hand fields according to existing backend models;
- operational loading/empty/error states.

No inventory mutation is added unless an existing browser-safe authorized command already supports the required action.

### 8. Orders

Provide:

- pageable order list;
- state badges and useful columns;
- supported filters/search only;
- order detail drill-down;
- navigation to fulfillment/payment/return context where identifiers exist;
- mutations only when supported by existing private contracts.

Do not expose internal persistence fields merely to make the screen appear richer.

### 9. Fulfillment

Provide:

- shipment/order context using real backend data;
- shipment state transitions through existing private commands;
- carrier/tracking fields where relevant;
- permission-aware transition controls;
- validation and backend error feedback;
- UI must not knowingly offer transitions that current state makes invalid.

### 10. Payments

Provide:

- paginated payment list;
- status filtering;
- provider/payment/order identifiers;
- clear ambiguous/reconciliation-related states where available.

No blind retry action is allowed. Payment ambiguity/reconciliation remains governed by backend reliability rules.

### 11. Returns

Provide a return workflow around the existing private commands:

- list/detail;
- approve;
- reject;
- receive;
- inspect;
- refund;
- permission-aware controls;
- confirmation for destructive/financially meaningful actions;
- validation for quantities/dispositions.

### 12. Settlement

Provide:

- seller-scoped settlement listing;
- state and amount context;
- approval with reason;
- authorization-aware controls.

Settlement business rules are not redesigned in this feature.

### 13. Moderation

The current Admin Moderation page can read reviews/comments but uses the normal user report API as a substitute for moderator action. That is not sufficient.

The current Comment backend already owns these moderator use cases:

- `POST /internal/comments/{threadId}/hide` with `COMMENT_MODERATE`;
- `POST /internal/comments/{threadId}/unhide` with `COMMENT_MODERATE`.

Feature 015 MUST expose browser-safe private equivalents for these same existing use cases, using:

- `POST /private/comments/{threadId}/hide`;
- `POST /private/comments/{threadId}/unhide`;
- `COMMENT_MODERATE` authorization;
- typed request/response contracts already consistent with Comment ownership;
- the same application/domain behavior rather than duplicating moderation logic;
- audit history for protected moderation actions when required by repository audit rules.

The existing internal endpoints may remain for service-to-service/internal use; Angular must never call them.

Review currently has no distinct moderator mutation contract. Therefore Feature 015 treats Reviews as **read/triage only** unless implementation discovers an already-existing domain moderation use case. It MUST NOT invent a new Review moderation engine or lifecycle merely for UI symmetry.

Admin Moderation UX should show relevant review/comment context and allow Comment hide/unhide when authorized.

### 14. Security / authorization administration

Provide:

- user authorization lookup;
- readable roles/permissions/seller scopes;
- assign/remove role through existing APIs;
- explicit confirmation for role changes;
- authorization snapshot refresh after mutation;
- permission-aware controls.

No JWT editing or frontend-side privilege expansion.

### 15. Audit

Provide:

- pageable audit history;
- filters supported by backend, starting with resource type;
- readable actor/action/resource/timestamp/outcome presentation;
- read-only behavior;
- safe links to related admin pages where identifiers are available.

### 16. Operations / recovery

Provide:

- incidents with severity/status/source/aggregate context;
- recover and resolve actions through existing APIs;
- reason input and confirmation;
- permission-aware controls;
- truthful failure state;
- supplemental observability links.

No generic “retry everything” action.

## Shared frontend API boundary

Admin components should use `MarketplaceApiService` or deliberately extracted typed bounded-context facades instead of hand-building backend URLs.

Within the changed scope:

- remove avoidable direct `ApiService` usage from feature components;
- add typed models where missing;
- remove avoidable `any`;
- preserve standard API envelope/auth handling;
- do not create one mega `AdminService` that owns every bounded context.

If `MarketplaceApiService` is split, the implementation plan must justify the boundary and preserve bounded-context ownership.

## Backend scope allowed by Feature 015

Backend work is limited to genuine browser-flow gaps. Expected candidates are:

1. Comment hide/unhide private aliases defined above.
2. Seller list/search only if required to replace a raw-ID-only admin workflow and no valid private contract exists.
3. A narrowly scoped list/detail query enhancement only when an admin workflow cannot function through existing contracts.

Any backend addition must:

- use interface-driven controllers;
- live under `/private/**`;
- enforce permission-based authorization;
- use typed transport models;
- map transport/application boundaries according to project conventions;
- preserve Hexagonal/DDD ownership;
- avoid cross-service SQL/database access;
- use grouped search criteria for search/list APIs;
- be implemented with backend TDD;
- emit protected-operation audit history where repository rules require it.

## Explicit non-goals

Feature 015 MUST NOT:

- migrate the Angular frontend stack;
- redesign backend bounded contexts;
- introduce unrelated business capabilities merely to make pages fuller;
- build a BI/reporting platform;
- fabricate analytics or operational metrics;
- let Angular call `/internal/**`;
- introduce a global BFF solely for Admin UI convenience;
- add GraphQL for convenience;
- replace Keycloak/OIDC;
- duplicate backend authorization rules in Angular;
- add blind retries;
- add performance indexes/partitions outside the dedicated database performance labs;
- invent Promotion/Review/Payment/Inventory business behavior not already required by existing domain contracts.

## Visual system

Use the approved preview as the visual reference:

- light main content surface;
- dark navy sidebar;
- restrained blue accent for selected navigation and primary actions;
- compact cards/tables;
- subtle borders/shadows;
- operational data density;
- status badges with text plus visual treatment;
- consistent spacing and typography;
- responsive collapse/drawer behavior for narrow widths.

Do not copy generated placeholder values from the preview.

A normal page should generally contain:

1. page header and primary action;
2. real summary cards only when meaningful data exists;
3. search/filter toolbar;
4. main table/list;
5. detail/action panel where needed;
6. standardized state handling.

## Forms, tables, and actions

### Tables

Prefer:

- deterministic server-side pagination when available;
- meaningful identifiers;
- status and ownership context;
- timestamps;
- consistent row actions;
- accessible headings and keyboard focus;
- no huge whole-domain client fetch solely to simulate pagination.

### Forms/actions

- labels describe business meaning;
- validation errors appear near the relevant field;
- backend errors retain the standard error contract;
- duplicate submission is prevented while an action is in flight;
- destructive, financial, security, moderation, and recovery actions require explicit confirmation where appropriate;
- successful mutations update affected view state without unnecessary full-page reloads.

## Authentication and authorization flow

```text
Keycloak initialization
  -> authenticated browser session
  -> GET /private/me/authorization
  -> centralized admin navigation/permission configuration
  -> route guard
  -> page/action visibility
  -> backend authoritative @PreAuthorize check
```

Unauthenticated and forbidden states must remain distinct.

Permission requirements must not come from query parameters or mutable browser state supplied by the user.

## Data flow

```text
Admin route/component
        |
        v
Typed bounded-context frontend facade
        |
        v
ApiService auth/envelope transport
        |
        v
Gateway / public or private API
        |
        v
Inbound adapter -> application port -> domain -> outbound port
```

Independent dashboard reads may run in bounded parallel calls. One failed secondary widget should show a truthful degraded/error state rather than necessarily blanking the entire shell.

Mutation flow:

```text
User action
  -> frontend permission-visible control
  -> confirmation + validation
  -> private API command
  -> backend authorization + domain validation
  -> standard response/error
  -> refresh/update affected UI state
```

## Required UI states

Major admin pages must visibly support the relevant subset of:

- loading;
- success;
- empty result;
- validation failure;
- business error;
- forbidden;
- unauthorized/session expired;
- infrastructure/server failure.

Do not silently turn failed reads into successful empty data.

## Accessibility and responsive behavior

- semantic controls/forms;
- visible keyboard focus;
- meaningful headings and labels;
- no color-only status meaning;
- responsive sidebar/drawer;
- critical actions reachable at narrower desktop/tablet widths;
- evidence includes at least one narrow/mobile-width Admin state.

## TDD and verification strategy

Frontend unit tests remain optional by repository decision, but feature-specific contract/static verification and Playwright are required.

### Frontend RED candidates

Before implementation, add failing checks proving missing behavior, including:

- centralized permission-aware navigation does not yet exist;
- dashboard still uses raw `ApiService`/`any` and a hard-coded service count;
- admin shell responsibilities remain concentrated in the current large inline `AppComponent`;
- standardized forbidden/state handling is missing;
- browser-safe Comment hide/unhide contracts are missing;
- required Admin Playwright coverage is missing.

Then follow RED -> GREEN -> REFACTOR.

### Backend TDD

For every new private API:

1. write a failing test for the missing contract/behavior;
2. verify the test fails for the intended reason;
3. implement the minimum behavior;
4. run targeted tests green;
5. refactor while green;
6. include the module in full backend verification.

### Required automated verification

At minimum:

- feature-specific static/contract verifier;
- Admin production build;
- Storefront production build to catch shared-frontend regressions;
- full backend Maven `clean verify` when backend changes;
- applicable targeted backend tests;
- Playwright Admin smoke/E2E against real services and deterministic seed data;
- Docker image build for changed deployables;
- affected Compose configuration validation.

## Required Playwright core flows

Cover a representative core slice rather than every possible action:

1. authenticated Admin shell;
2. permission-aware navigation;
3. Dashboard real/degraded states;
4. Catalog list/search plus one permitted action;
5. Order -> Fulfillment navigation and safe state/action verification;
6. Moderation read plus Comment hide/unhide through the new private contract;
7. Security or Audit read flow;
8. Operations incident read and deterministic recovery/resolve flow when suitable seed data exists;
9. forbidden route/action state;
10. narrow/mobile-width navigation.

Tests run against real backend services. Mock screenshots do not satisfy evidence requirements.

## Screenshot evidence

Store fresh evidence under:

```text
.agent/reports/015-admin-console-core-flow/
```

Required screenshots:

- Dashboard desktop;
- Catalog management;
- Seller management;
- Order/Fulfillment;
- Payment or Settlement;
- Moderation;
- Security/Authorization;
- Audit or Operations;
- forbidden/unauthorized state;
- narrow/mobile Admin shell.

The report README must identify exact branch/commit, startup commands, test commands, and screenshot mapping.

## Documentation updates

Implementation must update:

- `frontend/COVERAGE-MATRIX.md`;
- `.agent/reports/015-admin-console-core-flow/README.md`;
- relevant ADR only if an architectural decision changes;
- relevant `usecase/` note when a new production-relevant case is discovered.

## Acceptance criteria

Feature 015 is accepted only when:

1. Admin uses a responsive shell matching the approved visual hierarchy and density.
2. Navigation is grouped and permission-aware.
3. Unauthorized and forbidden states are distinct.
4. Dashboard uses truthful typed backend data and contains no fabricated production-style metrics.
5. Seller management is a usable workflow and avoids raw-ID-only interaction when a reasonable browser-safe selection contract exists or is added within scope.
6. Catalog provides useful search/list/create/publish workflow with clear status UX.
7. Media, Pricing/Promotion, Inventory, Orders, Fulfillment, Payments, Returns, Settlement, Security, Audit, and Operations use a consistent operational interaction model.
8. Comment moderation uses browser-safe `POST /private/comments/{threadId}/hide` and `/unhide`, protected by `COMMENT_MODERATE`; Angular never calls the existing internal moderation endpoints.
9. Review moderation remains read/triage unless an existing domain moderator use case is discovered; no new Review moderation lifecycle is invented.
10. Browser code contains no `/internal/**` calls.
11. Changed Admin code uses typed API models/facades and removes avoidable `any`/raw URL construction.
12. Sensitive/destructive/financial/security/moderation/recovery actions use appropriate confirmation and preserve backend rules.
13. Loading, empty, error, unauthorized, and forbidden handling is consistent.
14. Admin and Storefront production builds pass.
15. Applicable backend tests and full backend verification pass for backend changes.
16. Playwright smoke/E2E passes against real services and deterministic data.
17. Required screenshots are captured and indexed under `.agent/reports/015-admin-console-core-flow/`.
18. `frontend/COVERAGE-MATRIX.md` is updated.
19. Final AI-assisted feature branch is squashed to exactly one logical commit on top of the intended `develop` base.
20. GitHub Actions is green for the exact final rewritten HEAD SHA before merge-ready handoff.
21. No unrelated business-domain implementation is introduced.

## Implementation boundary summary

Feature 015 intentionally makes:

- a **large UX/workflow quality improvement** in Admin;
- only **small targeted backend contract additions** where the browser is genuinely blocked;
- **no backend domain redesign**;
- **no frontend framework migration**;
- **no fake analytics**;
- **no internal endpoint bypass**.
