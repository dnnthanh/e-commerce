# Admin Console Core Flow Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Turn the existing Angular Admin application into a responsive, permission-aware marketplace operations console while exposing only the minimum browser-safe Comment moderation API required by the approved Feature 015 specification.

**Architecture:** Keep the existing Angular 22 multi-project frontend and shared `MarketplaceApiService` boundary. Extract the Admin shell, navigation metadata, and shared state/presentation primitives into focused Admin files; keep bounded-context pages using typed shared contracts. Backend changes are limited to exposing the existing Comment hide/unhide use cases through `/private/**` aliases guarded by `COMMENT_MODERATE`; no new moderation domain behavior is introduced.

**Tech Stack:** Angular 22, TypeScript 6, Keycloak JS 26, Spring Boot 4.1, Java 25, Maven, Playwright 1.55, Python static verification, GitHub Actions, Docker Compose.

## Global Constraints

- Use Angular; MUST NOT migrate to React, Next.js, Tailwind, Zustand, React Query, or another frontend stack.
- Browser code MUST NOT call `/internal/**`.
- Frontend permission checks are UX only; backend `@PreAuthorize` remains authoritative.
- No fabricated revenue, user, service-count, or trend metrics.
- Review moderation remains read/triage only; do not invent a Review moderation lifecycle.
- Do not add blind retry behavior, new performance indexes/partitions, GraphQL, or a cosmetic cross-domain BFF.
- Use `/private/**` for authenticated browser APIs and permission-based authorization.
- Frontend unit tests are optional by repository decision, but Feature 015 static/contract verification, production builds, real Playwright flows, and screenshots are required.
- Backend changes follow strict RED -> GREEN -> REFACTOR TDD.
- Final merge-ready branch must contain exactly one logical commit on top of `develop`, with CI green for that rewritten final SHA.

---

## File Map

### New focused Admin shell/shared files

- `frontend/projects/admin/src/app/admin-navigation.ts` — one permission-aware grouped navigation definition reused by routes and sidebar.
- `frontend/projects/admin/src/app/admin.routes.ts` — Admin route table and dedicated unauthorized/forbidden routes.
- `frontend/projects/admin/src/app/layout/admin-shell.component.ts` — responsive shell/top bar/sidebar and narrow-screen drawer state.
- `frontend/projects/admin/src/app/shared/admin-state.component.ts` — standardized loading/empty/error/forbidden/unauthorized presentation.
- `frontend/projects/admin/src/app/shared/admin-page-header.component.ts` — consistent page heading/eyebrow/actions surface.
- `frontend/projects/admin/src/app/shared/status-badge.component.ts` — textual status badge; never color-only meaning.
- `frontend/projects/admin/src/app/features/unauthorized.component.ts` — login/session-expired state.
- `frontend/projects/admin/src/app/features/forbidden.component.ts` — authenticated permission-denied state.

### Existing frontend files to modify

- `frontend/projects/admin/src/app/app.component.ts` — reduce to shell host; remove inline routing/navigation ownership.
- `frontend/projects/admin/src/main.ts` — import route table from `admin.routes.ts`.
- `frontend/projects/admin/src/styles.css` — approved navy/light visual system, responsive shell, tables/forms/states/focus.
- `frontend/shared/auth.guard.ts` — redirect auth failures to `/unauthorized` and permission failures to `/forbidden` while preserving backend authority.
- `frontend/shared/auth.service.ts` — expose identity/authorization UX helpers without expanding JWT authority.
- `frontend/shared/marketplace-api.service.ts` — add typed `hideComment`/`unhideComment`; keep all browser paths public or `/private/**`.
- `frontend/shared/marketplace-types.ts` — reuse/extend only concrete typed Admin view models needed by changed pages.
- `frontend/projects/admin/src/app/features/dashboard.component.ts` — remove raw `ApiService`, `any`, hard-coded `53`, and swallowed failures.
- `frontend/projects/admin/src/app/features/sellers.component.ts` — use authorization `sellerIds` as the primary seller selector and permission-aware editing.
- `frontend/projects/admin/src/app/features/catalog.component.ts` — consistent typed search/list/action states and permission-aware publish/create.
- `frontend/projects/admin/src/app/features/media.component.ts` — shared states and in-flight action behavior.
- `frontend/projects/admin/src/app/features/pricing.component.ts` — common Admin presentation; preserve diagnostic semantics.
- `frontend/projects/admin/src/app/features/promotion.component.ts` — common Admin presentation; keep evaluation-only behavior.
- `frontend/projects/admin/src/app/features/inventory.component.ts` — standardized filters/page/state rendering.
- `frontend/projects/admin/src/app/features/orders.component.ts` — pageable list and contextual navigation.
- `frontend/projects/admin/src/app/features/fulfillment.component.ts` — safe state transition controls and validation.
- `frontend/projects/admin/src/app/features/payments.component.ts` — status filter/table/state; no retry action.
- `frontend/projects/admin/src/app/features/returns.component.ts` — confirmation/validation around existing return commands.
- `frontend/projects/admin/src/app/features/settlements.component.ts` — seller scope selector, approval reason and permission checks.
- `frontend/projects/admin/src/app/features/moderation.component.ts` — Review read/triage + Comment hide/unhide only.
- `frontend/projects/admin/src/app/features/security.component.ts` — explicit role-change confirmation and authorization refresh.
- `frontend/projects/admin/src/app/features/audit.component.ts` — read-only standardized pageable/filter state.
- `frontend/projects/admin/src/app/features/operations.component.ts` — incident recover/resolve confirmation/reason and truthful errors.
- `frontend/COVERAGE-MATRIX.md` — mark Feature 015 Admin core routes and evidence.

### Backend files

- `backend/services/be-comment-api/src/test/java/com/dnnthanh/marketplace/be/comment/api/api/CommentApiContractTest.java` — RED contract for browser-safe moderator paths and permission.
- `backend/services/be-comment-api/src/main/java/com/dnnthanh/marketplace/be/comment/api/api/CommentApi.java` — expose private aliases on the existing hide/unhide methods; retain internal endpoints.
- `backend/services/be-comment-api/src/main/java/com/dnnthanh/marketplace/be/comment/api/adapter/in/web/CommentController.java` — no new domain logic; existing hide/unhide delegation remains the implementation.

### Verification/evidence files

- `verification/verify_admin_console_v15.py` — feature static contract gate.
- `frontend/playwright/admin-core-flow.spec.ts` — representative real Admin flow.
- `.agent/reports/015-admin-console-core-flow/README.md` — exact commands, SHAs, CI runs, screenshot paths, known environment limitations.

---

### Task 1: Establish RED contracts for Feature 015

**Files:**
- Create: `backend/services/be-comment-api/src/test/java/com/dnnthanh/marketplace/be/comment/api/api/CommentApiContractTest.java`
- Create: `verification/verify_admin_console_v15.py`

**Interfaces:**
- Consumes: current `CommentApi`, Admin source tree and `MarketplaceApiService`.
- Produces: failing contract checks that require `/private/comments/{threadId}/hide|unhide`, centralized navigation, no Admin `/internal/**`, no Dashboard hard-coded service count/raw `ApiService`/`any`, and a Feature 015 Playwright spec.

- [ ] **Step 1: Write the backend failing contract test**

Use reflection so the test compiles against the current interface and fails on behavior rather than compilation:

```java
@Test
void exposesBrowserSafeModerationCommandsWithModeratorPermission() {
    assertThat(postMappings(CommentApi.class))
            .contains("/private/comments/{threadId}/hide", "/private/comments/{threadId}/unhide");
    assertThat(preAuthorizeExpressions(CommentApi.class, "hide", "unhide"))
            .allMatch(expression -> expression.contains("COMMENT_MODERATE"));
}
```

The helper reads `@PostMapping.value()` from interface methods and `@PreAuthorize.value()` from `hide`/`unhide`.

- [ ] **Step 2: Write the feature static verifier**

The verifier must read repository files and fail with explicit messages for these missing contracts:

```python
require("frontend/projects/admin/src/app/admin-navigation.ts", "centralized Admin navigation")
require("frontend/projects/admin/src/app/layout/admin-shell.component.ts", "focused Admin shell")
require("frontend/playwright/admin-core-flow.spec.ts", "Admin Playwright core flow")
for path in admin_ts_files:
    reject(path, "/internal/", "browser Admin source must not call internal endpoints")
reject(dashboard, "53", "dashboard must not hard-code architecture count")
reject(dashboard, "ApiService", "dashboard must use typed MarketplaceApiService")
reject(dashboard, "signal<any", "dashboard must not use any-backed feature models")
require_text(marketplace_api, "/private/comments/${threadId}/hide", "typed hide Comment browser command")
require_text(marketplace_api, "/private/comments/${threadId}/unhide", "typed unhide Comment browser command")
```

- [ ] **Step 3: Verify RED**

Run locally when a checkout is available, otherwise push the RED-only commit and use the branch GitHub Actions `static-verification` and backend jobs as the execution evidence:

```bash
./mvnw -f backend/pom.xml -pl services/be-comment-api -am -Dtest=CommentApiContractTest test
python verification/verify_admin_console_v15.py
```

Expected: both fail specifically because private moderator aliases/Admin 015 files do not exist yet.

- [ ] **Step 4: Preserve RED evidence**

Record the failing run SHA/job URLs in `.agent/reports/015-admin-console-core-flow/README.md` before later history squash.

---

### Task 2: Expose browser-safe Comment moderator commands

**Files:**
- Modify: `backend/services/be-comment-api/src/main/java/com/dnnthanh/marketplace/be/comment/api/api/CommentApi.java`
- Verify unchanged delegation: `backend/services/be-comment-api/src/main/java/com/dnnthanh/marketplace/be/comment/api/adapter/in/web/CommentController.java`
- Test: `backend/services/be-comment-api/src/test/java/com/dnnthanh/marketplace/be/comment/api/api/CommentApiContractTest.java`

**Interfaces:**
- Consumes: existing `CommentThreadResponse hide(String threadId)` and `unhide(String threadId)` use cases.
- Produces: the same methods available at both existing internal paths and browser-safe private paths with `COMMENT_MODERATE`.

- [ ] **Step 1: Add minimum GREEN mappings**

Use one method per existing use case; do not duplicate command logic:

```java
@PostMapping({
    "/internal/comments/{threadId}/hide",
    "/private/comments/{threadId}/hide"
})
@PreAuthorize("@authorizationService.hasPermission('COMMENT_MODERATE')")
CommentThreadResponse hide(@PathVariable String threadId);

@PostMapping({
    "/internal/comments/{threadId}/unhide",
    "/private/comments/{threadId}/unhide"
})
@PreAuthorize("@authorizationService.hasPermission('COMMENT_MODERATE')")
CommentThreadResponse unhide(@PathVariable String threadId);
```

- [ ] **Step 2: Verify targeted GREEN**

```bash
./mvnw -f backend/pom.xml -pl services/be-comment-api -am -Dtest=CommentApiContractTest test
```

Expected: PASS. Existing Comment domain tests also remain green.

- [ ] **Step 3: Refactor only while green**

If annotation formatting or test helpers need cleanup, make no behavioral changes and rerun the targeted test.

---

### Task 3: Build centralized permission-aware Admin shell and state primitives

**Files:**
- Create: `frontend/projects/admin/src/app/admin-navigation.ts`
- Create: `frontend/projects/admin/src/app/admin.routes.ts`
- Create: `frontend/projects/admin/src/app/layout/admin-shell.component.ts`
- Create: `frontend/projects/admin/src/app/shared/admin-state.component.ts`
- Create: `frontend/projects/admin/src/app/shared/admin-page-header.component.ts`
- Create: `frontend/projects/admin/src/app/shared/status-badge.component.ts`
- Create: `frontend/projects/admin/src/app/features/unauthorized.component.ts`
- Create: `frontend/projects/admin/src/app/features/forbidden.component.ts`
- Modify: `frontend/projects/admin/src/app/app.component.ts`
- Modify: `frontend/projects/admin/src/main.ts`
- Modify: `frontend/projects/admin/src/styles.css`
- Modify: `frontend/shared/auth.guard.ts`

**Interfaces:**
- Produces:

```ts
export interface AdminNavItem {
  label: string;
  path: string;
  permission?: string;
}
export interface AdminNavGroup {
  label: string;
  items: readonly AdminNavItem[];
}
export const ADMIN_NAVIGATION: readonly AdminNavGroup[];
```

`AdminShellComponent` filters items with `!item.permission || auth.has(item.permission)` and never derives permissions from URL/query state.

- [ ] **Step 1: Make static verifier require the new shell boundary**

Add checks for centralized navigation and dedicated unauthorized/forbidden routes; run verifier and confirm it still fails.

- [ ] **Step 2: Implement navigation metadata and routes**

Group exactly as the approved IA: Overview; Marketplace; Commerce; Community; Governance. Keep the route permissions matching backend permissions already used by current guards.

- [ ] **Step 3: Implement the shell**

Move sidebar/topbar markup out of `AppComponent`. Add menu toggle with signal state, identity/role context, login/logout, active-route treatment, and permission-filtered groups.

- [ ] **Step 4: Implement shared state primitives**

`AdminStateComponent` accepts a state kind (`loading | empty | error | forbidden | unauthorized`), title/message/traceId, and optional retry output. `StatusBadgeComponent` always renders status text in addition to visual styling.

- [ ] **Step 5: Implement auth redirects**

`authGuard` returns `/unauthorized` for missing authentication; `permissionGuard` returns `/forbidden` for authenticated users lacking the permission. Do not change backend authorization semantics.

- [ ] **Step 6: Apply approved responsive visual system**

Use CSS only: dark navy rail, light workspace, compact top bar, dense panels/tables, subtle borders/shadows, visible `:focus-visible`, mobile drawer, horizontal table overflow, status labels with text.

- [ ] **Step 7: Verify shell GREEN**

```bash
cd frontend
npm install --no-audit --no-fund
npm run build:admin
npm run build:storefront
cd ..
python verification/verify_admin_console_v15.py
```

Expected: Angular builds; verifier progresses past shell checks.

---

### Task 4: Upgrade Dashboard and bounded-context Admin workflows

**Files:**
- Modify: all existing `frontend/projects/admin/src/app/features/*.component.ts` listed in the File Map except auth-state components created in Task 3.
- Modify as needed: `frontend/shared/marketplace-types.ts`
- Modify: `frontend/shared/marketplace-api.service.ts`

**Interfaces:**
- All pages consume `MarketplaceApiService` rather than hand-built URLs in feature components.
- `AuthService.has(permission)` controls action visibility only.
- Existing backend remains authoritative.

- [ ] **Step 1: Dashboard RED check**

Run static verifier against current Dashboard and confirm failures for raw `ApiService`, `signal<any[]>`, hard-coded `53`, and swallowed incident failures.

- [ ] **Step 2: Dashboard GREEN implementation**

Use typed `IncidentView[]` from `MarketplaceApiService.incidents()`. Cards are truthful: open incidents, critical/high incidents, current effective permissions count, current seller scopes count. A failed incidents widget renders degraded/error state; it is not converted to an empty-success state.

- [ ] **Step 3: Seller management**

Use `auth.authorization().sellerIds` as the primary selectable seller scope. Load `sellerShops(selectedSellerId)`, show shop status/context, validate nonblank edit name, disable duplicate saves while in flight, and show `SELLER_UPDATE` actions only when permitted. Do not create a global Seller-search endpoint for cosmetic convenience.

- [ ] **Step 4: Catalog/media/pricing/promotion**

Catalog keeps typed server pagination/search and existing draft/publish commands; publish requires confirmation and permission visibility. Media keeps existing upload session/completion behavior with clear in-flight/error states. Pricing/Promotion remain diagnostic and do not invent CRUD/business rules.

- [ ] **Step 5: Inventory/orders/fulfillment/payments**

Inventory exposes SKU/warehouse filters and typed balances. Orders keep pageable list/detail context and link to fulfillment when order numbers exist. Fulfillment only offers backend-supported transitions and validates carrier/tracking inputs where required. Payments expose filtering/state only; add no retry command.

- [ ] **Step 6: Returns/settlement/security/audit/operations**

Keep existing commands and add explicit confirmations/reasons for meaningful actions. Use seller scopes for settlement selection. Refresh authorization after role mutation. Audit stays read-only. Operations recover/resolve keeps reason and truthful failure states; no generic retry-all.

- [ ] **Step 7: Verify all changed frontend code**

```bash
cd frontend
npm run build:admin
npm run build:storefront
cd ..
python verification/verify_admin_console_v15.py
```

Expected: both builds pass and verifier has no raw Admin `/internal/**`, Dashboard hard-coded count, or required typed-boundary failures.

---

### Task 5: Implement real Comment moderation UX

**Files:**
- Modify: `frontend/shared/marketplace-api.service.ts`
- Modify: `frontend/projects/admin/src/app/features/moderation.component.ts`
- Modify as needed: `frontend/shared/marketplace-types.ts`

**Interfaces:**

```ts
hideComment(threadId: string): Promise<CommentThread>
unhideComment(threadId: string): Promise<CommentThread>
```

Both use `/private/comments/${threadId}/hide|unhide`.

- [ ] **Step 1: Add typed facade commands**

Implement exactly the two methods above; do not expose `/internal/**` in browser code.

- [ ] **Step 2: Replace report-as-moderation behavior**

Review list remains read/triage. Comment rows show Hide only when not hidden and Unhide only when hidden, guarded by `COMMENT_MODERATE`. Remove the current Admin `reportComment(... 'Admin triage report')` substitute.

- [ ] **Step 3: Update row state after successful mutation**

Replace the changed thread in the local signal from the command response rather than full-page reload. Disable the row action while pending and preserve backend error/trace information.

- [ ] **Step 4: Verify contract and build**

```bash
python verification/verify_admin_console_v15.py
cd frontend && npm run build:admin && cd ..
./mvnw -f backend/pom.xml -pl services/be-comment-api -am test
```

Expected: PASS.

---

### Task 6: Add real Playwright Admin core flow and evidence

**Files:**
- Create: `frontend/playwright/admin-core-flow.spec.ts`
- Modify only if required for deterministic auth/runtime setup: `frontend/playwright.config.ts`
- Modify: `frontend/COVERAGE-MATRIX.md`
- Create/update: `.agent/reports/015-admin-console-core-flow/README.md`

**Interfaces:**
- Playwright runs against real Compose services and deterministic seed data; it does not route/mock backend domain responses.

- [ ] **Step 1: Add representative core tests**

Cover:

```text
authenticated shell
permission-aware navigation
dashboard successful/degraded rendering
catalog list/search + one permitted action
order -> fulfillment contextual navigation
moderation read + comment hide/unhide
forbidden route state
narrow/mobile Admin shell
```

Use role/test-user credentials already provided by repository runtime configuration; never commit a new real secret.

- [ ] **Step 2: Capture screenshots from the real running Admin**

At minimum capture desktop dashboard, desktop data workflow, moderation state, forbidden state, and one narrow/mobile state. Store paths referenced by the report; do not fabricate screenshots.

- [ ] **Step 3: Update coverage matrix**

Mark only flows actually exercised by the real Playwright run.

- [ ] **Step 4: Run real UI verification**

```bash
./compose-up.sh --profile full up -d --build
cd frontend
npm run test:ui -- admin-core-flow.spec.ts
cd ..
./compose-up.sh --profile full down --remove-orphans -v
```

If the full profile is blocked by a previously documented unrelated runtime defect, run the smallest real service slice needed for the affected flow and document the exact blocker and executed slice in the report; do not claim an unrun full flow.

---

### Task 7: Full quality gate, squash, PR, and exact-SHA CI

**Files:**
- Update: `.agent/reports/015-admin-console-core-flow/README.md`
- Update spec status: `.agent/specs/015-admin-console-core-flow.md`

- [ ] **Step 1: Run full repository quality gate**

```bash
python verification/verify_admin_console_v15.py
for script in verification/verify_*.py; do python "$script"; done
./mvnw -f backend/pom.xml -B -ntp clean verify
cd frontend
npm install --no-audit --no-fund
npm run build:storefront
npm run build:admin
cd ..
docker compose config --quiet
docker compose --profile full --profile heavy --profile observability config --quiet
docker compose build admin
docker compose build backend-runtime-build
```

Record fresh command results in the report.

- [ ] **Step 2: Self-review against Feature 015 acceptance criteria**

Confirm every navigation/action permission, no browser `/internal/**`, truthful Dashboard data, Review read-only moderation semantics, Comment private hide/unhide, explicit unsafe-action confirmations, responsive states, typed APIs, and evidence coverage.

- [ ] **Step 3: Squash branch to one logical commit**

Rebuild one final tree containing spec + plan + source + tests + reports with parent equal to the current `develop` HEAD. Force-update `feature/admin-console-core-flow` to that single commit only after all intended files are present.

Commit message:

```text
feat: deliver admin console core flow
```

- [ ] **Step 4: Verify compare metadata**

Compare `develop...feature/admin-console-core-flow`; expected `ahead_by = 1` and only Feature 015 files/intentional shared files changed.

- [ ] **Step 5: Open PR into `develop`**

PR title: `feat: deliver admin console core flow`.

PR body must summarize scope, backend private moderation aliases, TDD RED/GREEN evidence, frontend/Playwright evidence, screenshots, and verification commands.

- [ ] **Step 6: Read GitHub Actions for the exact squashed SHA**

Required CI jobs: static verification, backend `clean verify`, storefront/admin builds, Compose validation, Docker builds, core infrastructure smoke. If a job fails, inspect logs, fix only the root cause, re-squash/rewrite to one logical commit, and rerun all required checks against the new final SHA.

- [ ] **Step 7: Final handoff only when exact final SHA is green**

Report final SHA, compare commit count, PR URL/number, required CI status, screenshots/evidence paths, and any genuinely external/non-feature limitation. Do not call the branch merge-ready before that point.
