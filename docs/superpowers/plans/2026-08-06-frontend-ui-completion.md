# Frontend UI Completion Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Turn the existing Angular storefront and admin applications from API/integration demos into realistic, routed, responsive marketplace experiences while preserving the current backend boundaries, Keycloak authorization model, and MSA architecture.

**Architecture:** Keep both Angular 22 applications standalone and signal-based. Preserve the current transport boundary (`ApiService`) and bounded-context facade (`MarketplaceApiService`); browser code must never call `/internal/**`. Split the oversized application shells and inline page templates into route files, layout components, reusable presentational components, and feature pages. Add Playwright smoke/E2E coverage for real user flows because repository policy does not require frontend unit tests but does require real UI flow evidence.

**Tech Stack:** Angular 22, TypeScript 6, Angular Router, Angular signals, RxJS 7, Keycloak JS 26, CSS design tokens, Playwright for E2E, existing Docker Compose local stack.

## Global Constraints

- Keep `storefront` and `admin` as separate Angular applications under `frontend/projects/`.
- Do not introduce NgRx unless a later measured state-management problem justifies it; use signals/services first.
- Continue using `frontend/shared/api.service.ts` for transport and `frontend/shared/marketplace-api.service.ts` for bounded-context orchestration.
- Browser code must never call `/internal/**`.
- Authentication remains Keycloak/OIDC through `frontend/shared/auth.service.ts`.
- Authorization-aware routes/actions use `authGuard`, `permissionGuard`, and `PermissionDirective`; backend authorization remains authoritative.
- Reuse `frontend/shared/marketplace-types.ts` for shared API-facing view types; do not scatter `any`/duplicate local interfaces when a stable contract exists.
- Every page must intentionally render loading, empty, error, unauthorized/forbidden (where applicable), and success states.
- Mobile and desktop layouts are both acceptance targets; minimum smoke widths are 390px and 1440px.
- Storefront and admin production builds must stay green after every milestone.
- Update `frontend/COVERAGE-MATRIX.md` whenever a flow moves from diagnostic/demo state to a completed UI flow.
- Do not hide backend contract gaps in frontend code. If a safe public/private API is missing, create the backend contract first rather than calling an internal endpoint.
- Each implementation milestone should use its own feature branch/spec even though this document is the umbrella plan.

---

## Current-State Findings

The repository already has business routes and feature components. The main gap is not route count; it is product-quality UI structure and interaction depth.

Storefront currently exposes routes for home, search, product detail, cart, checkout, orders, order detail, returns, notifications, community, seller shop, and account directly from `frontend/projects/storefront/src/app/app.component.ts`.

Admin currently exposes dashboard, seller, catalog, media, pricing, promotion, inventory, orders, fulfillment, payment, return, moderation, settlement, security, audit, and operations routes directly from `frontend/projects/admin/src/app/app.component.ts`.

Most pages use large inline templates and direct page-local loading logic. There is no Playwright/Cypress test harness in `frontend/package.json`, and the Angular workspace currently has build targets only.

Two deliberate backend/UI contract gaps are already documented in `frontend/COVERAGE-MATRIX.md`:

1. Public Catalog product responses do not yet expose a complete sellable SKU/offer projection.
2. Some moderation mutations are internal-only and therefore cannot be wired to browser admin UI until permission-protected APIs exist.

---

# Delivery Roadmap

Implement the UI in six independently reviewable milestones. Do not attempt all pages in one branch.

1. **UI-001 — Frontend foundation and design system**
2. **UI-002 — Storefront discovery and product experience**
3. **UI-003 — Storefront purchase and account journeys**
4. **UI-004 — Admin shell and commerce management**
5. **UI-005 — Admin governance and recovery operations**
6. **UI-006 — E2E, accessibility, responsive polish, and evidence**

The milestones below are ordered. UI-002+ depend on the primitives and route structure introduced by UI-001.

---

### Task 1: UI-001 — Establish a maintainable Angular application structure

**Files:**
- Modify: `frontend/projects/storefront/src/app/app.component.ts`
- Create: `frontend/projects/storefront/src/app/app.routes.ts`
- Create: `frontend/projects/storefront/src/app/layout/storefront-shell.component.ts`
- Modify: `frontend/projects/admin/src/app/app.component.ts`
- Create: `frontend/projects/admin/src/app/app.routes.ts`
- Create: `frontend/projects/admin/src/app/layout/admin-shell.component.ts`
- Modify: `frontend/projects/storefront/src/main.ts`
- Modify: `frontend/projects/admin/src/main.ts`

**Interfaces:**
- Produces storefront `routes: Routes` from `app.routes.ts`.
- Produces admin `routes: Routes` from `app.routes.ts`.
- `AppComponent` becomes a minimal root host; navigation/layout belongs to shell components.

- [ ] **Step 1: Move route definitions out of both `AppComponent` files**

Storefront routes must preserve the existing paths and guards:

```ts
export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'search', component: SearchComponent },
  { path: 'product/:id', component: ProductDetailComponent },
  { path: 'cart', component: CartComponent, canActivate: [authGuard] },
  { path: 'checkout', component: CheckoutComponent, canActivate: [authGuard] },
  { path: 'orders', component: OrdersComponent, canActivate: [authGuard] },
  { path: 'orders/:orderNo', component: OrderDetailComponent, canActivate: [authGuard] },
  { path: 'returns', component: ReturnsComponent, canActivate: [authGuard] },
  { path: 'notifications', component: NotificationsComponent, canActivate: [authGuard] },
  { path: 'product/:id/community', component: CommunityComponent },
  { path: 'shops/:shopId', component: SellerShopComponent },
  { path: 'account', component: AccountComponent, canActivate: [authGuard] },
  { path: '**', redirectTo: '' }
];
```

Admin routes must retain the current permission guards rather than replacing them with role checks.

- [ ] **Step 2: Extract the storefront header/footer and admin sidebar/header into layout components**

The root component should be reduced to a route host, for example:

```ts
@Component({
  standalone: true,
  selector: 'app-root',
  imports: [StorefrontShellComponent],
  template: '<market-storefront-shell />'
})
export class AppComponent {}
```

- [ ] **Step 3: Keep authentication UI in the shell and business UI in feature pages**

The shell may consume `AuthService`; product/order feature components must not be responsible for rendering global login/logout navigation.

- [ ] **Step 4: Build both applications**

Run:

```bash
cd frontend
npm ci
npm run build:storefront
npm run build:admin
```

Expected: both commands exit `0`.

- [ ] **Step 5: Commit the structural extraction**

```bash
git add frontend/projects/storefront/src/app frontend/projects/admin/src/app frontend/projects/*/src/main.ts
git commit -m "refactor(frontend): extract application routes and shells"
```

---

### Task 2: UI-001 — Add shared design tokens and reusable UI-state components

**Files:**
- Create: `frontend/shared/ui/page-state.component.ts`
- Create: `frontend/shared/ui/loading-skeleton.component.ts`
- Create: `frontend/shared/ui/empty-state.component.ts`
- Create: `frontend/shared/ui/error-state.component.ts`
- Create: `frontend/shared/ui/price.component.ts`
- Create: `frontend/shared/ui/status-badge.component.ts`
- Modify: `frontend/projects/storefront/src/styles.css`
- Modify: `frontend/projects/admin/src/styles.css`
- Modify: representative existing pages under `frontend/projects/*/src/app/features/`

**Interfaces:**
- `PageStateComponent` accepts `loading`, `error`, and `empty` inputs and projects success content.
- `PriceComponent` renders VND consistently.
- `StatusBadgeComponent` maps business status text to semantic appearance without encoding domain transitions.

- [ ] **Step 1: Define design tokens in both application stylesheets**

Use shared semantic variable names for spacing, radius, typography, surfaces, borders, focus rings, success/warning/danger/info states, and responsive breakpoints. Keep brand-specific values inside each application's stylesheet rather than importing a large third-party UI framework.

- [ ] **Step 2: Implement reusable loading/empty/error components**

`PageStateComponent` must support retry through an output event:

```ts
@Component({ selector: 'market-page-state', standalone: true, templateUrl: './page-state.component.html' })
export class PageStateComponent {
  readonly loading = input(false);
  readonly error = input('');
  readonly empty = input(false);
  readonly retry = output<void>();
}
```

- [ ] **Step 3: Replace at least Home and Admin Dashboard ad-hoc state markup with the shared components**

Use this as the pattern before migrating all later pages.

- [ ] **Step 4: Verify keyboard focus is visible for links, buttons, form fields, and sidebar navigation**

Use CSS `:focus-visible`; never remove browser focus without an accessible replacement.

- [ ] **Step 5: Build both apps and commit**

```bash
cd frontend
npm run build:storefront
npm run build:admin
git add shared projects/storefront/src/styles.css projects/admin/src/styles.css projects/*/src/app/features
git commit -m "feat(frontend): add shared UI primitives and design tokens"
```

---

### Task 3: UI-001 — Introduce browser E2E infrastructure before feature expansion

**Files:**
- Modify: `frontend/package.json`
- Modify: `frontend/package-lock.json`
- Create: `frontend/playwright.config.ts`
- Create: `frontend/e2e/storefront/public-discovery.spec.ts`
- Create: `frontend/e2e/admin/auth-shell.spec.ts`
- Modify: `.github/workflows/ci.yml`

**Interfaces:**
- `npm run e2e:storefront` executes storefront smoke tests.
- `npm run e2e:admin` executes admin smoke tests.
- Tests use public/local runtime URLs from environment variables rather than hard-coded production hosts.

- [ ] **Step 1: Add Playwright**

Add `@playwright/test` as a dev dependency and scripts:

```json
{
  "e2e:storefront": "playwright test e2e/storefront",
  "e2e:admin": "playwright test e2e/admin",
  "e2e": "playwright test"
}
```

- [ ] **Step 2: Configure two projects**

Use `STORE_FRONT_URL` defaulting to `http://127.0.0.1:4200` and `ADMIN_URL` defaulting to `http://127.0.0.1:4201`. Capture screenshots and traces on failure.

- [ ] **Step 3: Add a public storefront smoke test**

The test must verify home renders, navigation reaches `/search`, and product cards or the documented empty state are visible.

- [ ] **Step 4: Add an admin unauthenticated shell test**

Verify the admin application renders the login action and does not expose protected page content without authentication.

- [ ] **Step 5: Keep CI lightweight**

Do not resurrect a single full-stack runner. Run frontend build on every PR; run runtime E2E only in a targeted job that starts the minimum required profile/services or on a dedicated sufficiently sized runner.

- [ ] **Step 6: Commit E2E foundation**

```bash
git add frontend/package*.json frontend/playwright.config.ts frontend/e2e .github/workflows/ci.yml
git commit -m "test(frontend): add Playwright smoke foundation"
```

---

### Task 4: UI-002 — Complete storefront home, search, filters, and category-style discovery

**Files:**
- Modify: `frontend/projects/storefront/src/app/features/home.component.ts`
- Modify: `frontend/projects/storefront/src/app/features/search.component.ts`
- Create: `frontend/projects/storefront/src/app/components/product-card.component.ts`
- Create: `frontend/projects/storefront/src/app/components/product-grid.component.ts`
- Create: `frontend/projects/storefront/src/app/components/search-filters.component.ts`
- Create: `frontend/projects/storefront/src/app/components/search-sort.component.ts`
- Modify: `frontend/shared/marketplace-api.service.ts`
- Modify: `frontend/shared/marketplace-types.ts`
- Modify: `frontend/e2e/storefront/public-discovery.spec.ts`

**Interfaces:**
- Search state is reflected in query parameters so refresh/back/forward preserve filters.
- `ProductCardComponent` consumes a typed product/search projection, never raw `any`.

- [ ] **Step 1: Model the search view contract in `marketplace-types.ts`**

Use the actual public Search/Catalog response fields. Do not invent fields that require internal APIs.

- [ ] **Step 2: Make search filters URL-driven**

At minimum preserve query text, seller/category/brand when supported, price range when supported, sort, and cursor/page state in router query params.

- [ ] **Step 3: Replace duplicated product card markup with `ProductCardComponent`**

The card must support media fallback, product name, seller/shop identity if public, price state, and navigation to `/product/:id`.

- [ ] **Step 4: Add responsive filter UX**

Desktop: side/filter bar. Mobile: collapsible filter panel. The same query-param state powers both.

- [ ] **Step 5: Add E2E coverage**

Verify search term entry updates results/URL, filters survive refresh, empty results render an empty state, and a result opens product detail.

- [ ] **Step 6: Build and commit**

```bash
cd frontend
npm run build:storefront
git add projects/storefront shared e2e/storefront
git commit -m "feat(storefront): complete discovery and search experience"
```

---

### Task 5: UI-002 — Complete product detail and seller shop experience

**Files:**
- Modify: `frontend/projects/storefront/src/app/features/product-detail.component.ts`
- Modify: `frontend/projects/storefront/src/app/features/seller-shop.component.ts`
- Create: `frontend/projects/storefront/src/app/components/product-gallery.component.ts`
- Create: `frontend/projects/storefront/src/app/components/offer-panel.component.ts`
- Create: `frontend/projects/storefront/src/app/components/rating-summary.component.ts`
- Create: `frontend/projects/storefront/src/app/components/seller-summary.component.ts`
- Modify: `frontend/shared/marketplace-api.service.ts`
- Modify: `frontend/COVERAGE-MATRIX.md`
- Modify: `frontend/e2e/storefront/public-discovery.spec.ts`

**Interfaces:**
- Product detail may show only permission-safe/public sellable information.
- Add-to-cart must consume a public sellable SKU/offer identifier, not an internal SKU-owner endpoint.

- [ ] **Step 1: Resolve the documented public offer-projection gap before hiding it with UI logic**

If the current public Catalog/Pricing APIs still cannot supply a sellable SKU/offer projection, add a backend feature/spec for that contract first. The frontend must not guess or call `/internal/**`.

- [ ] **Step 2: Compose the page from gallery, product information, offer/purchase panel, seller summary, and community preview**

Keep API orchestration in `MarketplaceApiService`; presentational components receive typed inputs.

- [ ] **Step 3: Add robust media fallback**

No broken-image icons. Missing media renders a deterministic placeholder.

- [ ] **Step 4: Complete seller shop route**

Show public seller/shop metadata and product list only from public APIs; follow/unfollow actions require authentication.

- [ ] **Step 5: Add E2E tests**

Verify product page renders from a search result, seller shop navigation works, and authenticated-only purchase/follow actions redirect to login when anonymous.

- [ ] **Step 6: Update `COVERAGE-MATRIX.md`, build, and commit**

```bash
cd frontend
npm run build:storefront
git add projects/storefront shared COVERAGE-MATRIX.md e2e/storefront
git commit -m "feat(storefront): complete product and seller experience"
```

---

### Task 6: UI-003 — Turn cart and checkout into a coherent purchase journey

**Files:**
- Modify: `frontend/projects/storefront/src/app/features/cart.component.ts`
- Modify: `frontend/projects/storefront/src/app/features/checkout.component.ts`
- Create: `frontend/projects/storefront/src/app/components/cart-line.component.ts`
- Create: `frontend/projects/storefront/src/app/components/order-summary.component.ts`
- Create: `frontend/projects/storefront/src/app/components/checkout-stepper.component.ts`
- Modify: `frontend/shared/marketplace-api.service.ts`
- Create: `frontend/e2e/storefront/purchase-flow.spec.ts`

**Interfaces:**
- Cart updates preserve the backend cart version/concurrency contract.
- Checkout always revalidates/reprices through backend APIs; displayed cart price is not treated as final authorization for payment.

- [ ] **Step 1: Present cart lines with quantity, save-for-later/remove actions, line state, and optimistic-action feedback**

Do not silently overwrite a version conflict. Render a recoverable message and refresh the authoritative cart.

- [ ] **Step 2: Render checkout as explicit stages**

Stages: review cart → delivery/customer information supported by API → promotion/price validation → payment provider selection → submit → outcome.

- [ ] **Step 3: Disable duplicate submit while checkout/payment initiation is pending**

The UI protection complements backend idempotency; it does not replace it.

- [ ] **Step 4: Treat ambiguous/processing payment outcomes honestly**

Show `processing/pending reconciliation` when backend state is not final; never display success based only on QR generation or provider redirection.

- [ ] **Step 5: Add purchase-flow E2E**

Cover cart load, quantity change, checkout validation, submit, and success/pending/failure result states using deterministic local seed/payment simulator data.

- [ ] **Step 6: Build and commit**

```bash
cd frontend
npm run build:storefront
git add projects/storefront shared e2e/storefront/purchase-flow.spec.ts
git commit -m "feat(storefront): complete cart and checkout journey"
```

---

### Task 7: UI-003 — Complete orders, tracking, returns, account, notification, and community flows

**Files:**
- Modify: `frontend/projects/storefront/src/app/features/orders.component.ts`
- Modify: `frontend/projects/storefront/src/app/features/order-detail.component.ts`
- Modify: `frontend/projects/storefront/src/app/features/returns.component.ts`
- Modify: `frontend/projects/storefront/src/app/features/account.component.ts`
- Modify: `frontend/projects/storefront/src/app/features/notifications.component.ts`
- Modify: `frontend/projects/storefront/src/app/features/community.component.ts`
- Modify: `frontend/shared/notification.service.ts`
- Create: `frontend/e2e/storefront/account-order-flow.spec.ts`

**Interfaces:**
- Order detail is the navigation hub for fulfillment/tracking, cancellation eligibility, payment state, and return creation.
- Notification realtime events refresh/merge with durable notification state; websocket/SSE delivery is not the source of truth.

- [ ] **Step 1: Add filterable order history with status/date filters supported by backend**

Use URL query parameters for filters; render a meaningful empty state.

- [ ] **Step 2: Build order timeline/detail**

Show seller splits/shipments/payment/fulfillment state from existing response contracts. Only render cancellation/return actions when the backend state permits them.

- [ ] **Step 3: Make return creation order-driven**

Do not require users to manually type IDs that are already known from order detail. Route or pass selected order/line context into the return flow.

- [ ] **Step 4: Complete account and notification preference screens**

Authorization snapshot may be shown as account/security information, but customer UX should not expose raw permission-debug data as the primary experience.

- [ ] **Step 5: Complete review/comment UI**

Support the browser-safe review/comment actions already represented in the coverage matrix: summary/list/create/helpful and comment/reply/react/report.

- [ ] **Step 6: Add E2E for authenticated account/order flow**

Use a deterministic test identity and seeded order. Verify order history → detail → tracking/return navigation and durable notification read state.

- [ ] **Step 7: Build and commit**

```bash
cd frontend
npm run build:storefront
git add projects/storefront shared e2e/storefront
git commit -m "feat(storefront): complete account order and community flows"
```

---

### Task 8: UI-004 — Make the admin shell permission-aware and operationally usable

**Files:**
- Modify: `frontend/projects/admin/src/app/layout/admin-shell.component.ts`
- Modify: `frontend/projects/admin/src/app/app.routes.ts`
- Create: `frontend/projects/admin/src/app/navigation/admin-navigation.ts`
- Modify: `frontend/shared/permission.directive.ts`
- Create: `frontend/e2e/admin/navigation-permissions.spec.ts`

**Interfaces:**
- `AdminNavigationItem` contains path, label, and required permission(s).
- Sidebar visibility and route guards use the same permission vocabulary; neither substitutes for backend checks.

- [ ] **Step 1: Replace the hard-coded sidebar with navigation metadata**

```ts
export interface AdminNavigationItem {
  path: string;
  label: string;
  permission?: string;
}
```

Populate it with the current route permissions (`SELLER_VIEW`, `PRODUCT_PUBLISH`, `MEDIA_UPLOAD`, `INVENTORY_VIEW`, `ORDER_VIEW`, `FULFILLMENT_VIEW`, `PAYMENT_VIEW`, `RETURN_VIEW`, `COMMENT_MODERATE`, `SETTLEMENT_VIEW`, `SECURITY_VIEW`, `AUDIT_VIEW`, `OPERATIONS_VIEW`).

- [ ] **Step 2: Hide unauthorized navigation items while keeping guarded routes**

Direct URL access must still be rejected by the route guard and backend.

- [ ] **Step 3: Add responsive admin navigation**

Desktop sidebar and mobile drawer must use the same navigation model.

- [ ] **Step 4: Add E2E permission tests**

Test at least one restricted identity and one admin/operator identity. Verify forbidden navigation is absent and direct route access is blocked.

- [ ] **Step 5: Build and commit**

```bash
cd frontend
npm run build:admin
git add projects/admin shared e2e/admin
git commit -m "feat(admin): add permission-aware application shell"
```

---

### Task 9: UI-004 — Complete admin commerce-management pages

**Files:**
- Modify feature pages under `frontend/projects/admin/src/app/features/`:
  - `catalog.component.ts`
  - `media.component.ts`
  - `pricing.component.ts`
  - `promotion.component.ts`
  - `inventory.component.ts`
  - `sellers.component.ts`
  - `orders.component.ts`
  - `fulfillment.component.ts`
  - `payments.component.ts`
  - `returns.component.ts`
  - `settlements.component.ts`
- Create reusable components under `frontend/projects/admin/src/app/components/`
- Modify: `frontend/shared/marketplace-api.service.ts`
- Modify: `frontend/shared/marketplace-types.ts`
- Create: `frontend/e2e/admin/commerce-operations.spec.ts`

**Interfaces:**
- List/search pages keep search/filter/page state in URL query parameters.
- Mutations are permission-gated in UI and enforced by backend.
- Tables provide loading, empty, error, and retry states and responsive alternatives where wide tables do not fit mobile widths.

- [ ] **Step 1: Standardize admin list pages**

Create reusable page-header, filter-bar, data-table/list-state, pagination/cursor, confirm-dialog, and action-feedback components instead of duplicating raw tables/buttons.

- [ ] **Step 2: Catalog/media**

Support product list/search/detail/edit/publish flows allowed by current APIs and media upload/readiness state. Do not expose direct object-storage credentials.

- [ ] **Step 3: Pricing/promotion**

Render effective dates/rules clearly, validate form inputs client-side for UX, and always display backend validation errors. Do not reproduce promotion/pricing business rules in Angular.

- [ ] **Step 4: Inventory/orders/fulfillment**

Make entity identifiers clickable between operational contexts. Surface reservation/stock/order/shipment states as timelines/badges rather than raw JSON.

- [ ] **Step 5: Payments/returns/settlements**

Distinguish final, pending, failed, refund, reconciliation, and settlement states. Dangerous actions require a confirmation dialog and render the returned authoritative state.

- [ ] **Step 6: Add E2E happy-path and permission-denied coverage for representative mutations**

Choose one representative mutation per bounded-context family rather than attempting exhaustive UI E2E for every endpoint.

- [ ] **Step 7: Build and commit**

```bash
cd frontend
npm run build:admin
git add projects/admin shared e2e/admin/commerce-operations.spec.ts
git commit -m "feat(admin): complete commerce management workflows"
```

---

### Task 10: UI-005 — Complete moderation, security, audit, and recovery center

**Files:**
- Modify: `frontend/projects/admin/src/app/features/moderation.component.ts`
- Modify: `frontend/projects/admin/src/app/features/security.component.ts`
- Modify: `frontend/projects/admin/src/app/features/audit.component.ts`
- Modify: `frontend/projects/admin/src/app/features/operations.component.ts`
- Modify: `frontend/projects/admin/src/app/features/dashboard.component.ts`
- Modify: `frontend/shared/marketplace-api.service.ts`
- Modify: `frontend/COVERAGE-MATRIX.md`
- Create: `frontend/e2e/admin/governance-recovery.spec.ts`

**Interfaces:**
- Recovery operations must display incident/current state before allowing replay/retry/manual resolution.
- Audit views are read-only immutable history from `be-audit` APIs.
- Moderation mutations require browser-safe private/admin APIs.

- [ ] **Step 1: Resolve moderation API gaps first**

If approve/reject/hide/restore moderation operations are only internal endpoints, implement permission-protected browser-facing APIs before wiring buttons. Update the coverage matrix when complete.

- [ ] **Step 2: Replace debug-centric security UI with permission/scope management views**

Show user identity, roles, effective permissions, seller/shop scopes, and explicit assignment/revocation actions supported by backend APIs.

- [ ] **Step 3: Make audit searchable and readable**

Provide grouped search criteria, result pagination, actor/action/resource/outcome columns, detail view for metadata, and no mutation controls.

- [ ] **Step 4: Make operations a recovery center rather than a raw incident table**

Expose DLT/replay, stuck Saga, reconciliation, and job/incident actions only where APIs exist. Confirmation dialogs must explain the operation and ambiguous outcomes.

- [ ] **Step 5: Make dashboard metrics dynamic or clearly label static architecture metadata**

Do not present hard-coded counts such as deployable totals as live telemetry unless sourced dynamically.

- [ ] **Step 6: E2E the recovery permission boundary**

Verify an unauthorized identity cannot see/execute a recovery action and an authorized identity sees the action plus resulting status feedback.

- [ ] **Step 7: Build and commit**

```bash
cd frontend
npm run build:admin
git add projects/admin shared COVERAGE-MATRIX.md e2e/admin/governance-recovery.spec.ts
git commit -m "feat(admin): complete governance and recovery workflows"
```

---

### Task 11: UI-006 — Apply responsive, accessibility, and failure-state polish across all pages

**Files:**
- Modify: `frontend/projects/storefront/src/styles.css`
- Modify: `frontend/projects/admin/src/styles.css`
- Modify feature/component templates under both Angular projects
- Create: `frontend/e2e/storefront/responsive-accessibility.spec.ts`
- Create: `frontend/e2e/admin/responsive-accessibility.spec.ts`

**Interfaces:**
- All page-level forms have associated labels and accessible validation messages.
- All interactive controls are keyboard reachable.
- Mobile layouts do not require horizontal page scrolling at 390px except intentionally scrollable data regions.

- [ ] **Step 1: Audit 390px and 1440px layouts**

Run the two applications and inspect every routed page at both widths. Fix overflow, unreadable tables, clipped dialogs, and navigation issues.

- [ ] **Step 2: Normalize UI states**

Every network-backed page must render explicit loading → success/empty/error transitions; unauthorized/forbidden pages must not look like generic network failures.

- [ ] **Step 3: Normalize form behavior**

Disable submission only when invalid/pending, show field-level validation, preserve user input after recoverable server errors, and move focus to the first relevant error after failed submit.

- [ ] **Step 4: Add basic automated accessibility assertions in Playwright**

At minimum assert page landmarks/headings, accessible names for form controls/buttons, and keyboard navigation for the primary shell paths. If adding an accessibility scanner library, keep it in E2E dev dependencies only.

- [ ] **Step 5: Build and run E2E**

```bash
cd frontend
npm run build:storefront
npm run build:admin
npm run e2e
```

Expected: production builds succeed and all configured E2E tests pass against the targeted local stack.

- [ ] **Step 6: Commit**

```bash
git add frontend
git commit -m "feat(frontend): polish responsive accessible UI states"
```

---

### Task 12: UI-006 — Capture completion evidence and update coverage

**Files:**
- Modify: `frontend/COVERAGE-MATRIX.md`
- Create: `.agent/reports/frontend-ui-completion/verification.md`
- Create screenshot evidence under `.agent/reports/frontend-ui-completion/screenshots/`

**Interfaces:**
- Evidence maps each completed flow to route, identity/permission context, backend contexts exercised, and verification command/test.

- [ ] **Step 1: Run production builds from a clean install**

```bash
cd frontend
rm -rf node_modules dist
npm ci
npm run build:storefront
npm run build:admin
```

- [ ] **Step 2: Start the required local stack using the canonical wrapper**

For standard storefront/admin flows:

```bash
./compose-up.sh --profile full up -d --build
```

Only add `--profile heavy` for UI scenarios that explicitly require Oracle/SQL Server-backed finance/order paths:

```bash
./compose-up.sh --profile full --profile heavy up -d --build
```

- [ ] **Step 3: Run E2E against the running stack**

```bash
cd frontend
npm run e2e
```

- [ ] **Step 4: Capture screenshot evidence**

Required sets:
- anonymous storefront home/search/product;
- authenticated cart/checkout/order/account;
- payment success/pending/failure where deterministically reproducible;
- notification/realtime state;
- admin dashboard plus representative commerce page;
- permission denied/hidden action;
- operations/recovery action;
- loading/empty/error state examples;
- 390px and 1440px representative layouts.

- [ ] **Step 5: Update `COVERAGE-MATRIX.md`**

For each row, state which route(s) now implement the flow and explicitly retain any backend contract gaps that remain.

- [ ] **Step 6: Write verification report and commit**

```bash
git add frontend/COVERAGE-MATRIX.md .agent/reports/frontend-ui-completion
git commit -m "docs(frontend): add UI completion verification evidence"
```

---

## Backend Contract Work That May Block UI Tasks

These are not permission to bypass service boundaries. Open dedicated backend feature specs when the UI reaches them.

### Public sellable offer projection

Current coverage documentation states that public Catalog product responses are not yet sufficient for a complete sellable SKU/offer choice. Before product detail/add-to-cart is considered complete, define a permission-safe public contract that supplies the IDs and display fields needed for selection and purchase.

Acceptance condition: storefront can select a valid sellable offer/SKU without calling `/internal/**` or asking the user to type a diagnostic SKU identifier.

### Moderation mutations

Current coverage documentation states that some moderation mutations are internal-only.

Acceptance condition: admin moderation actions call protected browser-facing endpoints guarded by explicit permissions; internal service-to-service endpoints remain inaccessible to the browser.

### API contract hygiene during UI work

When a page needs a field that is missing from its browser-safe response:

1. verify the field is genuinely part of that bounded context's public/private view;
2. update the owning backend API DTO/projection through its normal feature/spec/TDD workflow;
3. update `frontend/shared/marketplace-types.ts`;
4. update the UI;
5. add contract/integration evidence.

Do not compensate with cross-database access, internal endpoints, or client-side reconstruction of domain rules.

---

## Suggested Branch Sequence

```text
develop
├── feature/ui-foundation
├── feature/storefront-discovery
├── feature/storefront-purchase-account
├── feature/admin-commerce-ui
├── feature/admin-governance-ui
└── feature/frontend-e2e-polish
```

Each branch should start from the then-current `develop`, have its own spec/implementation evidence, pass the relevant CI gates, and be auto-squashed to one logical commit before it is reported merge-ready unless the user explicitly requests preserved history.

---

## Final Acceptance Criteria

UI completion is not achieved merely because every API has a button. The program is complete when:

- storefront and admin use routed shells and reusable UI components instead of giant inline application/page templates;
- discovery → product → cart → checkout → order is navigable without manually entering technical identifiers;
- account, notification, community, return, and tracking flows are usable from normal navigation;
- admin commerce and governance workflows expose only actions allowed by effective permissions;
- no browser code calls `/internal/**`;
- documented backend API gaps are resolved or explicitly remain open in the coverage matrix;
- every route has intentional loading/empty/error/forbidden behavior;
- representative flows work at 390px and 1440px;
- both Angular production builds pass;
- targeted Playwright E2E passes against the local stack;
- screenshots and verification evidence are stored under `.agent/reports/`.

## Self-Review Results

- **Coverage:** all rows in `frontend/COVERAGE-MATRIX.md` are addressed by UI-002 through UI-005; UI-001 and UI-006 provide shared structure and evidence.
- **Known gaps:** public sellable offer projection and browser-safe moderation mutations are explicitly treated as backend blockers, not frontend workarounds.
- **Testing:** frontend unit tests remain optional per repository policy; Playwright provides required real-flow smoke/E2E coverage.
- **CI/resource model:** plan preserves the MSA-sized CI strategy and does not reintroduce the removed all-in-one `full-stack-smoke` runner.
