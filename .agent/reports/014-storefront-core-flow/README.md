# Feature 014 — Storefront Core Flow Verification Evidence

## Scope

This evidence index covers the implementation of `.agent/specs/014-storefront-core-flow.md` on branch `feature/storefront-core-flow-implementation` / PR #3.

Implemented scope:

- public Catalog sellable-offer projection;
- routed/responsive storefront shell;
- Home, Search and Category discovery;
- Product Detail offer/variant selection without manual SKU input;
- customer-readable Cart preserving optimistic version semantics;
- cart-driven Checkout preserving stable idempotency and backend validation;
- Account, Order History and Order Detail/Tracking UX;
- shared loading/empty/error/auth states;
- storefront/admin production-build compatibility.

## TDD evidence

### Backend public offer contract — RED

Commit: `e448d7f7c45e2fe328c03cc30a73b84e6af56a98`

GitHub Actions run: `31112736184`

Backend job: `92654433744`

Observed failure was the intended feature-contract failure: `ProductOffer` / `ProductOfferPort` and offer use-case methods did not yet exist. No unrelated failure was used as RED evidence.

### Backend public offer contract — GREEN

After minimal implementation plus formatter correction, commit `f82bec77287009b064a23ae8c922ce0dc0f4b27b` produced a successful full backend Maven reactor in GitHub Actions run `31113245385`.

The three new `ProductServiceImplementTest` scenarios passed, including published-product visibility, draft-product non-leakage and missing/non-sellable SKU behavior.

### Frontend storefront contract — RED

Commit: `8780b97944d42548eacff43767376537e208067a`

GitHub Actions run: `31113550935`

The new `verification/verify_storefront_core_flow_v14.py` failed on the expected missing behavior: public offer facade methods, category route, real Product Detail offer selection and removal of diagnostic SKU input.

### Frontend storefront contract — GREEN

Implementation commit `52ab20a59efe2f2c911733e7aeceac37977ca723` moved the feature verifier to green. After correcting one Angular optional-boolean binding at commit `54d25c89b0c99cd05892de68a8529db8bb053206`, GitHub Actions run `31115185366` completed all repository CI jobs successfully:

- `static-verification`;
- `backend`;
- `frontend` (storefront + admin production builds);
- `compose-config`;
- `docker-build`;
- `core-infrastructure-smoke`.

## Review follow-up

A source review against the Order domain found two customer-UX contract issues before final history rewrite:

1. cancellation UI was too permissive compared with `MarketplaceOrder.cancel`, which only permits `CREATED` and `PAYMENT_PENDING`;
2. UI exposed cancellation reasons not present in the backend `CancellationReason` enum.

The UI now exposes customer cancellation only for `CREATED` / `PAYMENT_PENDING` and sends `CUSTOMER_REQUEST`.

Checkout was also tightened to execute `validateCart()` again immediately before submission and stop when backend validation reports `valid=false`.

`verify_storefront_core_flow_v14.py` now guards these behaviors in addition to the original offer/route/no-internal-endpoint checks.

## Final CI requirement

Per `AGENTS.MD`, all AI-created intermediate commits are squashed into one logical feature commit on top of `develop`. The required GitHub Actions jobs must be rerun against that exact rewritten HEAD before this branch can be considered CI-verified.

The PR checks are the canonical evidence for the exact final SHA; this report intentionally does not hard-code a final run id that would become stale if the final history rewrite changes the SHA.

## Runtime UI / screenshot evidence

**Status: BLOCKED in the current execution environment; not claimed as passed.**

The current ChatGPT execution host cannot obtain a local repository checkout from `github.com`, so it cannot run the canonical `./compose-up.sh up -d --build` flow or attach a real browser to the running storefront.

The repository's current CI intentionally uses a core-infrastructure smoke instead of the former very large full-stack runtime smoke. A real authenticated customer UI path requires the application stack including Keycloak, SQL Server order/fulfillment persistence, OpenSearch, MongoDB community services, Cart/Checkout/Pricing/Inventory/Payment APIs and gateway/storefront. Re-introducing that large stack into every PR CI run solely to manufacture screenshots would reverse the repository's current CI topology decision.

Therefore no mock/static screenshot is substituted for real evidence.

Still-open runtime evidence required by Feature 014:

- anonymous Home desktop;
- Search or Category results;
- Product Detail with a real offer selected;
- authenticated Product Detail -> Add to Cart;
- Cart;
- Checkout;
- checkout result / resulting Order Detail;
- Order Detail shipment/tracking state;
- one empty/error/auth-related state;
- one representative mobile-width storefront state.

Until those are captured from a real running implementation and stored under this report directory, PR #3 should remain Draft and must not be represented as fully merge-ready under the Feature 014 acceptance criteria.
