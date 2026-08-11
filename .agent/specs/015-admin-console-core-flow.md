# Feature 015 — Admin Console Core Flow

## Status

Approved visual direction; specification checkpoint before implementation planning.

## Context

Feature 014 completed the storefront core flow and was merged into `develop`. The next priority in `NEXT_WORK.md` is to turn the existing admin application from a collection of thin routed demo components into a realistic, authorization-aware marketplace operations console.

The current admin application already contains routes for dashboard, seller, catalog, media, pricing, promotion, inventory, orders, fulfillment, payments, returns, moderation, settlement, security, audit, and operations. The goal of this feature is not to invent those domains again. It is to make the existing admin surface coherent, navigable, operationally useful, visually polished, and safely integrated with the existing backend contracts.

The approved visual direction is the preview produced for Feature 015: a light, professional administration workspace with a dark navigation rail, compact top bar, operational summary cards, data-heavy tables, filters, status badges, and focused action panels. The preview is a visual reference only. The repository remains Angular-based; this feature MUST NOT migrate the frontend to React, Next.js, Tailwind, Zustand, or React Query.

## Goal

Deliver a realistic Admin Console core flow that:

- uses a responsive, reusable application shell;
- exposes only navigation and actions the authenticated user is authorized to use;
- turns the existing admin routes into coherent operational workflows;
- removes avoidable raw-ID-driven demo interactions where the required selectable data already exists;
- standardizes loading, empty, error, unauthorized, and forbidden states;
- uses typed shared API contracts and the existing bounded-context facade;
- adds only the private browser-safe backend contracts required to close genuine admin workflow gaps;
- never calls `/internal/**` from browser code;
- provides Playwright smoke/E2E coverage and screenshot evidence from an actually running admin application.

## Design principles

### 1. Operational console, not a marketing dashboard

The admin application is a control plane for marketplace operators, seller administrators, support users, moderators, security administrators, finance users, and operations users. The UI should optimize for finding entities, understanding state, taking authorized actions, and recovering from failures.

Large decorative charts are secondary. Operational tables, filters, state, ownership, timestamps, actions, and traceability are primary.

### 2. No fabricated data

The UI MUST NOT hard-code production-looking counters, revenue totals, service counts, or status distributions merely to match the preview.

Dashboard cards and summaries must come from real available backend data. If an aggregate is not available through a valid browser-safe contract, show a truthful alternative such as incident count, payment count, order count, return count, or an explicit unavailable/empty state rather than inventing a number.

### 3. Permission-aware UX, backend-enforced authorization

Angular uses `/private/me/authorization` to load the effective authorization snapshot after Keycloak authentication.

- Route guards remain a UX/navigation control.
- Navigation items MUST be hidden when their required permission is absent.
- Mutation buttons and bulk actions MUST be hidden or disabled when the corresponding permission is absent.
- Backend `@PreAuthorize` remains authoritative.
- Frontend permission checks MUST NOT be treated as a security boundary.
- A generic forbidden page/state must be available when navigation is denied.

### 4. Browser-safe contracts only

Angular MUST use public or `/private/**` contracts. It MUST NOT call `/internal/**` endpoints.

When an existing admin workflow is blocked because only an internal service contract exists, this feature may add the smallest permission-protected private/admin API required for that workflow. The new endpoint must preserve the existing hexagonal boundaries and business ownership.

### 5. Reuse before duplication

Repeated admin patterns should be extracted into reusable Angular UI primitives where they materially reduce duplication. Examples include:

- page header;
- summary/stat card;
- filter/search toolbar;
- state badge;
- empty/loading/error/forbidden state;
- confirmation/action panel;
- pagination controls;
- entity lookup/select control where data contracts allow it.

Do not build a generic component framework for hypothetical future needs.

## Scope

### In scope

#### Admin application shell

- responsive dark navigation rail matching the approved visual direction;
- compact top bar with environment label, authenticated identity/roles, and login/logout controls;
- grouped navigation areas rather than one flat list;
- permission-aware navigation visibility;
- active-route indication;
- responsive collapsed/mobile navigation behavior;
- route-level unauthorized/forbidden UX;
- shared page layout and state primitives.

Recommended navigation groups:

- Overview: Dashboard;
- Marketplace: Sellers, Catalog, Media, Pricing, Promotions, Inventory;
- Commerce: Orders, Fulfillment, Payments, Returns, Settlement;
- Community: Moderation;
- Governance: Security, Audit, Operations.

The exact labels may be refined during implementation without changing the architectural intent.

#### Dashboard

Replace the current demo-style dashboard with a real operational landing page.

It should provide:

- truthful KPI/summary cards backed by existing contracts;
- latest incidents or operational alerts;
- recent or high-value operational entities where useful;
- quick links to major admin workflows;
- external observability links where configured;
- no raw `any` models in feature code;
- no hard-coded architecture/service-count metrics.

The dashboard may compose several existing APIs in parallel when the amount of data is bounded and the UX benefits justify it. Do not introduce a new cross-domain backend aggregation service only for cosmetic dashboard numbers unless a concrete performance or ownership problem is proven.

#### Seller management

Improve the existing seller/shop route into a usable management flow:

- seller/shop lookup or search using available browser-safe contracts;
- seller identity and shop state summary;
- edit shop name/description where authorized;
- clear validation and save feedback;
- no unnecessary manual copy/paste of IDs when selectable entities are already available;
- seller-scoped actions respect effective authorization.

If the backend lacks a browser-safe seller search/list contract that is required to remove raw-ID-only usage, add the smallest appropriate private query API in the Seller bounded context.

#### Catalog management

Improve catalog management from the current thin create/search/publish component into a real product lifecycle page:

- product search/filter/list with deterministic pagination;
- seller/category context shown clearly;
- product state/status badges;
- create draft workflow;
- product detail/edit action surface where supported by existing contracts;
- publish action with permission-aware confirmation;
- media linkage/navigation where useful;
- reusable table and filter patterns;
- no fake category metadata.

Category selection may remain ID-based only when the repository still lacks a customer/admin-safe category metadata contract. The feature MUST NOT invent a frontend-owned category tree.

#### Media management

- list or inspect product media using existing contracts where available;
- start upload session;
- show upload/completion state;
- complete upload action;
- clear product/asset relationship;
- error and retry feedback that does not hide backend failure semantics.

#### Pricing and promotion

Turn the existing diagnostic pages into consistent admin workflows without changing pricing/promotion business semantics.

- preserve existing price calculation ownership;
- provide clearly labeled criteria inputs and results;
- use reusable form/state components;
- promotion evaluation remains diagnostic unless a browser-safe campaign CRUD contract already exists;
- do not invent campaign persistence or pricing rules in this frontend feature.

If campaign/promotion management is not supported by existing private APIs, the page should truthfully present evaluation/diagnostic capabilities rather than fake CRUD.

#### Inventory

- searchable/paginated balance view;
- SKU/warehouse filters;
- clear available/reserved/on-hand style fields according to existing backend models;
- no inventory mutation unless a valid private API and permission already exist;
- operational empty/error states.

#### Orders

- pageable order list with useful columns and state badges;
- search/filter capability limited to existing backend contracts unless a small browser-safe query enhancement is required;
- order detail drill-down;
- customer/seller/order state context;
- links into fulfillment/payment/return workflows where identifiers are available;
- mutation actions only when backend contracts support them.

Do not expose internal order persistence details merely to make the table look richer.

#### Fulfillment

- shipment lookup/list context from real order/shipment data;
- shipment state transitions using the existing private command;
- carrier/tracking fields where relevant;
- permission-aware transition actions;
- clear validation and backend error feedback;
- prevent UI from presenting transitions that the backend does not accept where state information is sufficient to know this.

#### Payments

- paginated payment list;
- status filtering;
- clear provider/payment/order identifiers;
- no dangerous blind retry action;
- ambiguous payment outcomes remain governed by backend reconciliation semantics;
- operational links to incidents/reconciliation where applicable.

#### Returns

- return list/detail workflow;
- approve/reject/receive/inspect/refund actions using the existing private contracts;
- permission-aware actions;
- clear transition/history context where available;
- confirmation for destructive or financially meaningful actions;
- validation for quantities/disposition inputs.

#### Settlement

- seller-scoped settlement listing;
- settlement state/amount context;
- approval action with reason;
- authorization-aware controls;
- no settlement rule changes in this feature.

#### Moderation

The current page can query reviews/comments and submit a normal user report, but direct moderator actions are blocked because moderation mutations are internal-only.

This feature MUST close that contract gap with the smallest appropriate browser-safe private moderation commands required for the approved admin workflow, for example hide/unhide or moderation decision actions already owned by the Comment/Review bounded contexts.

Requirements:

- private permission-protected endpoints;
- typed request/response contracts;
- no browser call to `/internal/**`;
- existing business ownership remains in the correct bounded context;
- immutable audit history emitted for protected moderation actions when required by existing audit rules;
- moderation queue/list/detail UX with status, reporter/context, and actions that match the implemented backend capability.

Do not invent a completely new moderation engine.

#### Security / authorization administration

- user authorization lookup;
- roles/permissions/seller scopes presented clearly;
- assign/remove role using existing APIs;
- action visibility controlled by security permissions;
- explicit confirmation for role changes;
- refresh the displayed authorization snapshot after mutation;
- no JWT editing or frontend-side privilege expansion.

#### Audit

- pageable audit history;
- filters supported by the existing backend, starting with resource type;
- readable actor/action/resource/timestamp/result presentation;
- audit entries are read-only;
- useful identifiers link to relevant admin pages where safe and available.

#### Operations / recovery

- incident list with severity/status/source/aggregate context;
- recover and resolve actions using existing APIs;
- reason input and confirmation;
- permission-aware actions;
- preserve existing backend recovery/idempotency semantics;
- no generic “retry everything” control;
- external observability links remain supplemental, not a replacement for operational state.

#### Shared frontend API layer

- admin components should use `MarketplaceApiService` or a deliberately extracted typed bounded-context facade rather than hand-building URLs;
- feature code should not depend directly on raw `ApiService` unless a narrow platform-level reason exists and is documented;
- add missing typed models to `marketplace-types` or bounded-context frontend models;
- remove avoidable `any` from the admin feature path;
- preserve the shared API envelope handling conventions introduced by earlier features.

#### Frontend coverage matrix

Update `frontend/COVERAGE-MATRIX.md` so the Admin/operator column reflects the completed workflows and explicitly records any deliberate remaining API gaps.

### Backend scope allowed only when required by Admin browser flows

Backend changes are constrained to closing genuine browser-safe contract gaps required by the admin UX. Candidate gaps discovered before implementation include:

- moderation commands currently available only through internal contracts;
- seller search/list if raw seller ID entry cannot be replaced through an existing private contract;
- narrowly scoped list/detail query enhancement when an existing admin page cannot function without it.

Every backend addition must:

- use interface-driven controllers;
- live under `/private/**`;
- enforce permission-based authorization;
- map transport DTOs to application models at the boundary;
- preserve Hexagonal/DDD ownership;
- avoid cross-service database access;
- use grouped search request/criteria models for search/filter/list endpoints;
- include unit/integration tests where applicable;
- emit protected-operation audit history when required by repository rules.

## Explicit non-goals

This feature MUST NOT:

- migrate Angular to React, Next.js, Tailwind, Zustand, React Query, or another frontend stack;
- redesign backend bounded contexts;
- introduce unrelated business capabilities just to fill UI space;
- implement missing Ledger, shipping-provider, payment-provider, promotion-engine, or seller-domain behavior unrelated to the admin workflow;
- build a new BI/reporting platform;
- fabricate analytics/revenue metrics;
- bypass `/private/**` by calling `/internal/**` from Angular;
- introduce a global backend-for-frontend service solely for this UI;
- introduce GraphQL for convenience;
- replace Keycloak/OIDC authorization;
- duplicate backend permission logic in the frontend;
- add generic blind retries to operational actions;
- introduce production indexes/partitions merely for admin list screens before the database performance labs.

## Information architecture and visual system

### Visual direction

Use the approved Feature 015 preview as the visual reference:

- light main workspace;
- dark navy navigation rail;
- restrained blue accent for active navigation and primary actions;
- compact card and table surfaces;
- subtle borders/shadows;
- readable density suitable for operational data;
- green/yellow/red state treatments for success/warning/error-like statuses;
- consistent typography and spacing;
- desktop-first admin density with responsive collapse for narrower screens.

Do not attempt a pixel-for-pixel copy of generated placeholder values. The preview establishes layout, hierarchy, density, and tone—not fake content.

### Page composition

A standard admin page should normally follow:

1. page header: title, supporting context, primary action;
2. optional summary cards when real aggregate data exists;
3. filter/search toolbar;
4. primary data table/list;
5. secondary/detail/action panel when required;
6. standardized loading/empty/error/forbidden state.

### Tables

Operational tables should favor:

- deterministic ordering;
- server-side pagination when backend supports it;
- clear entity identifiers;
- status badge;
- ownership/seller context where relevant;
- meaningful timestamps;
- row-level actions grouped consistently;
- accessible focus and keyboard behavior;
- no huge client-side fetch of entire domains just to simulate pagination.

### Forms and actions

- labels must describe business meaning rather than implementation names;
- validation errors should appear near the relevant field and preserve backend error messages through the standard error contract;
- financially/security-sensitive or destructive actions require explicit confirmation;
- actions should present progress and disable duplicate submission while in flight;
- success feedback should update the relevant view rather than require a full-page reload where practical.

## Authentication and authorization behavior

### Startup

1. Angular initializes Keycloak once through the existing `AuthService`.
2. If authenticated, the frontend loads `/private/me/authorization`.
3. The shell derives visible navigation from effective permissions.
4. Route guards block unauthorized routes.
5. Pages derive action visibility from effective permissions.

### Forbidden/unauthorized distinction

- unauthenticated access should lead to login/auth UX;
- authenticated but unauthorized access should show a dedicated forbidden state;
- a failed backend authorization response must not be rewritten as a generic network error.

### Permission configuration

Permission-to-route/action mapping should be centralized in a small admin navigation/action configuration rather than scattered copies of string arrays across templates.

Do not dynamically trust permissions supplied by route query parameters or browser state.

## Data flow

```text
Admin route/component
        |
        v
Typed admin/shared frontend facade
        |
        v
ApiService envelope/auth transport
        |
        v
API Gateway / private backend API
        |
        v
Inbound adapter -> application port -> domain -> outbound port
```

For pages that need multiple independent read models, Angular may issue bounded parallel requests with explicit partial/error handling. A failure in one secondary dashboard widget should not necessarily blank the entire admin shell.

Mutation flow:

```text
User action
  -> frontend permission-visible action
  -> confirm/validate
  -> private API command
  -> backend authoritative authorization + domain validation
  -> standard response/error contract
  -> refresh/update affected UI state
```

## Error and state handling

All major admin pages must exercise and visibly support:

- initial loading;
- successful data state;
- empty result;
- validation failure;
- backend business error;
- forbidden response;
- unauthorized/session-expired response;
- transient infrastructure/server failure.

Avoid swallowing exceptions and replacing them with empty data unless the UI explicitly labels the degraded/failed widget. The current dashboard behavior of silently converting incident load failure to `[]` should be replaced with a truthful state.

## Accessibility and responsive behavior

- semantic buttons/links/forms;
- visible keyboard focus;
- meaningful table headings and labels;
- no color-only status communication;
- sidebar collapses or becomes a drawer on narrow widths;
- critical actions remain reachable on smaller desktop/tablet widths;
- screenshot verification must include at least one narrow/mobile-width admin state.

## TDD and testing strategy

### Frontend

Frontend unit tests are not mandatory by project decision, but contract/static verifiers and Playwright smoke/E2E are required.

Before implementation, add failing verification that proves the missing admin structure/behavior. Candidate RED checks:

- permission-aware navigation configuration does not yet exist;
- dashboard still uses raw `ApiService`/`any`/hard-coded count;
- shell is still a large inline `AppComponent`;
- standardized forbidden/state components do not yet exist;
- moderation browser-safe admin command contract does not yet exist;
- key workflows lack Playwright coverage.

Implementation then follows RED -> GREEN -> REFACTOR.

### Backend

Any new private API must follow normal backend TDD:

1. write a failing controller/application/domain/integration test for the required behavior;
2. verify the failure is for the missing contract/behavior;
3. implement the minimum code required;
4. run targeted test green;
5. refactor while keeping the test green;
6. include the module in full backend verification.

### Required automated verification

At minimum:

- feature-specific static/contract verifier;
- admin production build;
- storefront production build to prove shared frontend changes did not regress it;
- full backend Maven `clean verify` when backend code changes;
- applicable targeted backend integration tests;
- Playwright admin smoke/E2E against real services for completed core flows;
- Docker image build for admin and any changed backend deployables;
- Compose config validation for affected profiles.

## Playwright core flow expectations

The Playwright suite should cover a representative slice, not every possible admin action.

Required flows should include:

1. authenticated admin shell loads;
2. permission-aware navigation renders correctly for the test identity;
3. dashboard loads real operational data/state;
4. catalog search/list and one permitted catalog action;
5. order -> fulfillment navigation and one safe state/action verification;
6. moderation queue/detail and one moderator action once the private contract exists;
7. security or audit read flow;
8. operations incident read and safe recovery/resolve path when deterministic test data exists;
9. forbidden route/action state;
10. narrow/mobile-width navigation behavior.

Tests must run against real backend services and deterministic seed data. Do not replace required evidence with mocked screenshots.

## Screenshot evidence

Store fresh evidence under:

```text
.agent/reports/015-admin-console-core-flow/
```

Required screenshots:

- dashboard desktop;
- catalog management;
- seller management;
- order/fulfillment workflow;
- payment or settlement view;
- moderation view;
- security or authorization view;
- audit or operations view;
- forbidden/unauthorized state;
- narrow/mobile-width admin shell.

The report README must identify the exact tested branch/commit, startup commands, test commands, and screenshot file mapping.

## Documentation updates

Implementation must update:

- `frontend/COVERAGE-MATRIX.md`;
- `.agent/reports/015-admin-console-core-flow/README.md`;
- relevant ADR only if an architectural decision changes;
- relevant use-case note when a new production-relevant design case is discovered.

Do not create an ADR merely for CSS/layout choices.

## Quality guardrails

- Angular standalone-component conventions remain valid unless the implementation plan justifies a focused routing/component organization improvement.
- Break the current large admin `AppComponent` into shell/routing/navigation responsibilities; do not replace it with another giant component.
- Avoid giant template strings for complex pages. Use `.html`/`.css` files or smaller focused components when page complexity warrants it.
- Keep route definitions readable and permission mapping centralized.
- Do not create one mega `AdminService` that owns every bounded context.
- Preserve `MarketplaceApiService` as the bounded-context-aware facade or split it only when the implementation plan demonstrates that size/responsibility now justifies smaller typed facades.
- No raw `Map` transport models or untyped `any` in newly written admin workflow code unless an external library forces it and the boundary is documented.
- No direct repository/database access from controllers.
- No hard-coded backend service URLs in Angular or Java.
- No secrets in frontend runtime config or repository files.

## Acceptance criteria

Feature 015 is accepted only when all of the following are true:

1. Admin uses a responsive shell matching the approved visual hierarchy and density.
2. Navigation is grouped and permission-aware.
3. Unauthorized and forbidden states are distinct and user-visible.
4. Dashboard uses truthful typed backend data and contains no fabricated production-style metrics.
5. Seller management is a real routed workflow and avoids raw-ID-only interaction where a selectable contract exists or is added within scope.
6. Catalog supports useful search/list/create/publish management with clear status/state UX.
7. Media, pricing/promotion, inventory, orders, fulfillment, payments, returns, settlement, security, audit, and operations use a consistent admin interaction model.
8. Moderation no longer requires the browser to use a normal user report as a substitute for moderator action; required browser-safe private moderation command(s) exist and are permission protected.
9. Browser code contains no `/internal/**` calls.
10. Admin feature code uses typed API models/facades and removes avoidable `any`/raw URL construction in the changed scope.
11. Sensitive/destructive/financial/security actions require appropriate confirmation and preserve backend authorization/business rules.
12. Loading, empty, error, unauthorized, and forbidden states are implemented consistently.
13. Admin and storefront production builds pass.
14. Applicable backend tests and full backend verification pass for any backend changes.
15. Playwright smoke/E2E runs against real services and deterministic data.
16. Required screenshots are captured from the running application and indexed in `.agent/reports/015-admin-console-core-flow/`.
17. `frontend/COVERAGE-MATRIX.md` is updated.
18. Final AI-assisted feature branch is squashed to exactly one logical commit on top of the intended `develop` base before merge-ready handoff.
19. GitHub Actions is green for the exact rewritten final HEAD SHA.
20. No unrelated business domain implementation is introduced.

## Implementation boundary summary

The implementation should feel like a substantial Admin Console upgrade while remaining architecturally conservative:

- **big change in UX structure and workflow quality;**
- **small, targeted backend contract additions only where the browser is genuinely blocked;**
- **no backend domain redesign;**
- **no frontend framework migration;**
- **no fake analytics;**
- **no internal endpoint bypasses.**

This keeps Feature 015 aligned with the marketplace roadmap: a realistic operator experience built on the existing production-oriented service architecture rather than a parallel demo system.