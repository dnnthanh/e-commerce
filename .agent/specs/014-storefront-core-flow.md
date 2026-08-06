# Feature Spec 014 — Storefront Core Flow

## Status

**Approved for implementation — 2026-08-06.**

This spec formalizes the approved storefront direction before implementation begins. The implementation branch is `feature/storefront-core-flow`, created from `develop` at `1f9cb7a8ef0889f2d9bdb99e5d963f72e6861548`.

Implementation has **not** started at the time this spec is created.

## Problem

The repository already exposes broad backend coverage for discovery, catalog, pricing, cart, checkout, order, fulfillment, community, notification and account flows, and the Angular storefront can exercise many of those endpoints. However, the customer experience still behaves largely like a backend diagnostic client rather than a production-oriented marketplace storefront.

Key issues:

1. The customer journey is fragmented across diagnostic-style screens instead of one coherent flow from discovery to purchase and post-purchase management.
2. `AppComponent` currently owns too much page-shell/navigation/routing responsibility, which makes further storefront growth harder to maintain.
3. Home/search/category/product pages do not yet present the information hierarchy, reusable product cards, states and responsive behavior expected from a marketplace UI.
4. Product detail cannot naturally choose a sellable SKU/offer because the public Catalog contract does not yet expose a complete permission-safe SKU/offer projection. The UI therefore exposes a diagnostic SKU input for pricing/cart actions.
5. Cart and checkout expose technical details that should remain implementation concerns rather than primary customer interaction.
6. Loading, empty, error, unauthorized and forbidden states are not yet expressed consistently across the storefront.
7. The storefront has broad API coverage but lacks fresh end-to-end UI evidence proving the main anonymous and authenticated customer journeys.

The feature must turn the existing breadth into a coherent customer-facing storefront without bypassing bounded-context boundaries or pulling unrelated platform/refactor work into the branch.

## Goals

1. Deliver one coherent customer journey:

   ```text
   Home / Search / Category
       -> Product Detail
       -> Cart
       -> Checkout
       -> Order Success / Orders
       -> Order Detail / Tracking
   ```

2. Replace diagnostic-oriented storefront UI with production-oriented marketplace presentation and interactions.
3. Extract a maintainable storefront shell and routing structure from the current root component.
4. Introduce reusable storefront UI primitives only where they directly reduce duplication in the core flow.
5. Remove manual SKU-id entry from the normal product purchase path by adding a safe public sellable SKU/offer projection in Catalog.
6. Keep backend changes limited to contracts that directly block the storefront flow.
7. Preserve the existing shared frontend HTTP/error contract and browser rule that `/internal/**` is never called from Angular.
8. Keep Admin build/runtime behavior green while changing shared/frontend code.
9. Produce fresh build, smoke/E2E and screenshot evidence for the implemented customer journey.

## Selected delivery approach

The feature uses a **vertical customer-journey approach** rather than design-system-first or backend-contract-first delivery.

Implementation should complete each customer-visible slice in journey order while creating only the shared components/contracts required by that slice:

1. Storefront shell and route structure.
2. Home/search/category discovery.
3. Product detail and public sellable-offer contract.
4. Cart.
5. Checkout.
6. Account/orders/post-purchase.
7. Cross-cutting states, responsive polish and verification.

This approach is selected because the repository already has broad backend capability. The highest value now is a usable end-to-end storefront, not another layer of abstract UI infrastructure or a broad backend rewrite.

## Non-goals

This feature does **not**:

- build or redesign the Admin console;
- create a large standalone design system;
- redesign payment architecture or provider semantics;
- redesign Saga, inventory, pricing or promotion domain models;
- expose existing `/internal/**` contracts directly to the browser;
- add internal-only moderation mutations to Angular;
- perform broad observability/reliability refactoring;
- perform database performance/index/partition work;
- implement new business workflows for returns, settlement, KYC or unrelated bounded contexts;
- replace existing backend contracts merely for naming/style consistency when they already satisfy the storefront;
- treat mocked/static browser data as proof that an integration is complete.

## Storefront information architecture

The core storefront must support the following customer-facing routes/areas. Exact Angular route file decomposition belongs in the implementation plan, but the user-visible information architecture is contractual.

### Anonymous-capable discovery

- Home
- Search results
- Category results
- Product detail
- Public shop/seller view when supported by existing public contracts

### Authenticated purchase and account

- Cart
- Checkout
- Checkout result/order success
- Account summary
- Order history
- Order detail
- Shipment/tracking view integrated into order detail or a dedicated child view

Existing secondary routes such as returns, reviews/comments and notifications may remain available, but this feature only polishes them when required to make the core journey coherent.

## Storefront shell and navigation

The storefront root must become a real application shell rather than a combined shell + page implementation.

Required behavior:

- responsive top-level header;
- marketplace/home navigation entry;
- search entry that routes into discovery results;
- cart entry with a useful count/indicator when the current state makes it available;
- account/auth actions appropriate for anonymous vs authenticated state;
- desktop and mobile-friendly navigation behavior;
- router outlet/content region;
- lightweight footer;
- no diagnostic navigation language exposed as the main customer experience.

Routing and feature-page declarations must be moved out of the root component when doing so improves maintainability. The exact standalone-component/module organization should follow current Angular 22 conventions already used by the repository.

## Shared storefront UI primitives

Create only reusable components that are exercised by at least one core-flow page and are likely to be reused immediately.

Expected candidates include:

- `ProductCard`
- `PriceDisplay`
- `QuantityStepper`
- loading state
- empty state
- error state
- authorization/forbidden state where appropriate

A component must encapsulate presentation/interactions, not business rules that belong in backend/domain services.

Do not create a generic component framework merely to satisfy this section.

## Home, search and category discovery

### Home

Home should present a marketplace-oriented landing experience using real repository contracts/data where available. It should include a useful combination of:

- hero/primary discovery area;
- category shortcuts;
- product sections or discovery results;
- clear links into product/category/search journeys.

The implementation may adapt to the seed data that actually exists. It must not hard-code fake product objects only to make screenshots look populated.

### Search

Search must use the existing Search/Discovery public API and cursor/search-after semantics.

Required UX:

- keyword input;
- submitted search reflected in route/query state;
- reusable product result cards;
- supported filter/sort controls based on existing contract capability;
- cursor-based next-page/load-more behavior without pretending the backend is offset pageable;
- loading, empty and error states;
- deterministic navigation to product detail.

### Category

Category browsing must have a customer-facing route such as `/category/:id` or an equivalent route that preserves category identity in navigation.

Use existing public catalog/search contracts first. Add a backend contract only if a verified gap prevents category discovery; do not add a parallel category/search stack merely for UI convenience.

## Product detail

Product detail is the primary customer purchase decision page.

Required presentation, when data exists through public contracts:

- product title/name;
- media/gallery and responsive image variants;
- effective/current price;
- original/list price and promotion indicator when supplied by Pricing;
- review summary/rating;
- seller/shop information;
- variant/SKU selection;
- availability/stock state at a customer-appropriate level;
- quantity selection;
- Add to Cart action;
- existing useful review/comment/community content may remain below the purchase area.

The normal purchase flow must **not** ask the user to type a SKU identifier.

Diagnostic inputs may remain only behind an explicitly non-customer diagnostic/development surface if there is a strong reason; they must not be part of the default product purchase experience.

## Public sellable SKU/offer projection

### Contract gap

The existing frontend coverage documentation records that Catalog does not yet expose a complete public SKU/offer projection. This feature closes that gap instead of calling an internal catalog endpoint from Angular.

### Ownership

Catalog owns product/SKU identity and sellability metadata.

Pricing remains the owner of effective monetary calculation. Inventory remains the owner of inventory/reservation truth. Catalog must not copy those bounded contexts' business rules into its public projection.

### Required projection semantics

For a public product, the browser must be able to discover the currently selectable product variants/offers required to continue into Pricing and Cart.

The projection must expose only customer-safe data, including the minimum useful subset of:

- product identifier;
- SKU identifier;
- seller/shop identifier when required by downstream pricing/cart contracts;
- SKU code/display label where customer-visible;
- variant/attribute selections required to distinguish SKUs;
- lifecycle/sellability indicator necessary to hide non-sellable choices;
- any stable metadata required by the current Pricing/Cart input contracts.

It must not expose internal ownership, persistence or administrative fields merely because they exist in the Catalog model.

### Endpoint shape

The implementation plan must confirm the best fit against the existing Catalog public controller structure. A preferred shape is a public product child resource, for example:

```http
GET /products/{productId}/offers
```

or an equivalent existing public Catalog resource naming convention.

The exact path may differ if the current public API already has a more natural extension point, but the semantics above are mandatory.

### Integration rules

- Angular uses the public Catalog projection only.
- Angular then calls existing Pricing/Cart contracts with the selected identifiers.
- Catalog does not synthesize final price.
- Catalog does not expose `/internal/**` response types directly.
- Transport DTOs remain separate from application/domain/persistence types.
- Mapping follows the repository MapStruct/application-port conventions.
- Public endpoint remains authentication-free unless existing product visibility rules require otherwise.

## Cart

Cart must behave like a customer cart rather than an API workbench.

Required behavior:

- Add to Cart originates from a selected product SKU/offer.
- Existing optimistic/versioned cart semantics remain intact.
- Lines show customer-recognizable product/variant information.
- Lines are grouped by seller/shop when the cart/domain response supports that grouping.
- Quantity can be changed with a normal quantity control.
- Remove and save-for-later actions remain available where already supported.
- Validation/repricing messages are presented as customer-readable states while preserving stable backend error codes for debugging.
- Cart subtotal/summary and Checkout CTA are visible.
- Technical identifiers and expected-version fields are not primary manual customer inputs.

The frontend must not weaken optimistic concurrency or silently retry conflicting mutations in a way that hides a real version conflict.

## Checkout

Checkout must be driven from current cart state and selected cart lines, not from a standalone diagnostic form.

Required sections, subject to existing backend contract support:

- customer/delivery information;
- selected items grouped by seller/shop when appropriate;
- pricing/promotion summary;
- promotion-code entry/evaluation if already supported;
- payment-provider selection if already supported by the checkout/payment simulator path;
- final validation/reprice behavior;
- idempotent order/checkout submission;
- clear success result that links to the created order;
- clear recoverable/error state when checkout fails.

This feature must preserve the backend's idempotency and persistent Saga semantics. The UI must not generate repeated submissions accidentally while a checkout request is in flight.

## Account and orders

### Account

The account page should present useful authenticated-user information from the existing authorization/account snapshot rather than raw JSON diagnostics.

It may include:

- display identity information available to the frontend;
- relevant roles/permissions/scopes only when useful to explain available UX;
- links to orders, notifications and other existing customer areas.

### Order history

Order history must present customer-readable order cards/rows with:

- order identifier/reference;
- creation time;
- monetary summary;
- status;
- seller/order grouping information where useful;
- navigation to detail.

### Order detail and tracking

Order detail must present the current order/fulfillment contract in a customer-oriented structure:

- order summary;
- seller orders/line items;
- monetary snapshots;
- fulfillment/shipment status;
- tracking timeline/details when available;
- cancellation action only when allowed by the existing contract/state machine.

The UI must not invent unsupported state transitions.

## Authentication and authorization UX

- Public discovery/product routes remain usable anonymously.
- Cart/checkout/account/order routes that require identity must integrate with the existing Keycloak/auth store and route protection.
- The browser may use the loaded authorization snapshot to hide/disable irrelevant UI, but backend authorization remains authoritative.
- Unauthorized/expired-session handling must route the user toward authentication without exposing raw tokens.
- Forbidden errors must be distinguishable from anonymous/unauthenticated state.
- No controller/backend authorization rule is weakened solely to make storefront navigation easier.

## Error, loading and empty-state behavior

Every core page must deliberately handle the following applicable states:

- initial loading;
- background/action loading;
- empty data;
- validation error;
- backend business error;
- unauthorized;
- forbidden;
- not found;
- transient/infrastructure failure.

The shared `ApiClientError` contract remains the source for stable backend error code, localized message, details, status and `traceId`. Customer-facing copy may be simplified, but trace/error context must remain available for troubleshooting without exposing stack traces or secrets.

## Responsive and accessibility baseline

The feature must provide a practical responsive baseline for desktop and mobile-width storefront usage.

Minimum expectations:

- no essential controls become unreachable at narrow viewport widths;
- product cards and grids reflow sensibly;
- product purchase controls remain usable on mobile widths;
- forms have explicit labels or accessible names;
- buttons/links use semantic elements;
- keyboard focus is not intentionally trapped;
- images use meaningful alt text when content images convey product information;
- loading/action states communicate disabled/in-progress behavior where duplicate submission would be harmful.

This is a baseline, not a claim of full WCAG certification.

## Backend-change policy for this branch

Backend work is allowed only when one of the following is true:

1. a verified contract gap blocks the approved storefront journey;
2. an existing endpoint has a reproducible defect that prevents the journey from completing;
3. a small projection/query addition is required to present existing domain state safely to the public UI.

The known expected backend change is the public Catalog sellable SKU/offer projection.

Any Pricing, Promotion, Search, Cart, Checkout, Payment or Order runtime defect discovered during implementation must be handled as follows:

- fix it in this branch only if it blocks the core journey and the fix is narrow/coherent with this feature;
- otherwise record it as follow-up work instead of expanding this branch indefinitely.

No browser workaround may bypass service boundaries.

## Frontend architecture constraints

- Continue using `shared/ApiService` for the standard response/error/trace contract.
- Continue using the bounded-context facade (`MarketplaceApiService` or its current successor) rather than scattering raw endpoint strings through page components.
- Page components own presentation/orchestration only; reusable backend interaction belongs in the existing facade/service layer.
- Do not call `/internal/**` from Angular.
- Do not duplicate backend business calculations in TypeScript.
- Keep shared frontend changes compatible with both storefront and admin builds.
- Prefer strongly typed request/response models over `any`/unstructured objects.
- Preserve cursor pagination semantics for Search and pageable semantics for relational APIs.

## Testing strategy

Frontend unit tests remain optional by repository decision. This feature instead requires strong build and real-flow verification.

### Backend tests

For every backend contract changed by this feature:

- add/update unit tests for application/domain behavior;
- add adapter/controller contract tests as appropriate;
- add integration tests when persistence/query or security behavior is materially involved;
- verify public-vs-internal contract separation;
- verify non-sellable/private SKUs are not leaked through the public projection.

### Frontend static/build gates

At minimum:

```bash
cd frontend
npm install
npm run build:storefront
npm run build:admin
```

and the repository frontend contract/syntax verification commands documented by the frontend project must remain green.

### Storefront smoke/E2E scenarios

The final verification must exercise at least:

1. anonymous Home -> Search/Category -> Product Detail;
2. product variant/SKU selection without manually typing a SKU id;
3. authenticated Product Detail -> Add to Cart;
4. Cart -> quantity/update/validation -> Checkout;
5. Checkout -> success/order result for the local supported payment path;
6. Orders -> Order Detail -> Shipment/tracking visibility;
7. at least one empty/not-found/error state;
8. authentication redirect/guard behavior for a protected route;
9. storefront behavior at a representative mobile viewport;
10. Admin production build after shared frontend changes.

If an external/local dependency makes a scenario impossible in the execution environment, the evidence must state the exact blocker and must not claim the scenario passed.

## Screenshot evidence

Fresh screenshots of actual running pages must be stored/indexed under `.agent/reports/014-storefront-core-flow/`.

Minimum evidence set:

- Home desktop;
- search/category result;
- Product Detail with variant selected;
- Cart;
- Checkout;
- order success or resulting order detail;
- Order Detail/tracking;
- one empty/error/permission-related state;
- one representative mobile-width storefront state.

Screenshots must come from the running implementation and real local seed/API data, not static mockups.

## Documentation updates

Implementation completion must update:

- `frontend/README.md` when route/build/usage information materially changes;
- `frontend/COVERAGE-MATRIX.md` to remove the public SKU/offer gap once verified and to record any remaining deliberate gaps;
- this spec if an approved semantic requirement changes;
- an ADR only if implementation introduces a significant architectural choice with real alternatives/trade-offs;
- `usecase/` when a new production-relevant case is discovered that deserves a learning note under repository rules.

## Acceptance criteria

The feature is accepted only when all of the following are true:

1. `feature/storefront-core-flow` contains the approved implementation on top of `develop`.
2. The default storefront experience is a coherent marketplace journey rather than a collection of API diagnostics.
3. The root storefront component is reduced to an appropriate shell/bootstrap responsibility; navigation/routing/page responsibilities are maintainably separated.
4. Home/search/category routes let a customer discover products using real backend data.
5. Product Detail displays customer-relevant product/media/price/seller/review information available through public contracts.
6. A customer can select a real sellable SKU/variant from public data without typing a SKU id.
7. The browser does not call any `/internal/**` endpoint.
8. Catalog exposes a permission-safe public sellable SKU/offer projection without copying Pricing or Inventory business ownership.
9. Add to Cart works from the selected offer and preserves existing cart version semantics.
10. Cart presents real line information, quantity actions, validation and checkout entry without requiring manual technical identifiers.
11. Checkout is cart-driven, idempotent and presents a clear result.
12. Authenticated users can navigate order history and order detail/tracking using the existing domain contracts.
13. Loading, empty, error and auth-related states are intentionally handled across the core journey.
14. The storefront has a usable desktop and mobile-width responsive baseline.
15. `npm run build:storefront` passes on the final implementation SHA.
16. `npm run build:admin` passes on the final implementation SHA.
17. Applicable backend tests and repository static/contract gates pass on the final implementation SHA.
18. Required smoke/E2E scenarios are executed with fresh evidence or are explicitly documented as environment-blocked.
19. Fresh screenshot evidence is stored/indexed under `.agent/reports/014-storefront-core-flow/`.
20. `frontend/COVERAGE-MATRIX.md` accurately describes remaining deliberate gaps after implementation.
21. Before final merge-ready handoff, AI-created branch history is squashed to the single logical feature commit required by `AGENTS.MD`, and all required CI checks are rerun against that exact rewritten HEAD.

## Implementation boundary

After this spec is committed and reviewed, the next step is to create a detailed implementation plan that identifies:

- exact Angular files/components/routes to change;
- the exact Catalog public offer endpoint and DTO/application/domain/query path;
- TDD test order for the backend contract;
- vertical storefront implementation checkpoints;
- local Docker/seed prerequisites for E2E;
- verification commands and evidence paths.

No production implementation should begin before that implementation plan is written against the current feature-branch state.
