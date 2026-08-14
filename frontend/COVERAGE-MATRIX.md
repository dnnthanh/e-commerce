# Frontend ↔ backend coverage matrix

The frontend is intentionally split into transport (`ApiService`), bounded-context facade (`MarketplaceApiService`) and routed feature components. Browser code never calls `/internal/**`.

| Business flow | Storefront | Admin/operator | Backend contexts exercised |
| --- | --- | --- | --- |
| Discovery / product 360 | home, category, search cursor, product detail, public sellable offers, price, media, review/comment | catalog, media, pricing | Search, Catalog, Pricing, Media, Review, Comment |
| Seller relationship | follow seller, public shop route when `shopId` is available | seller shop management | Seller, Notification |
| Cart / checkout | versioned cart enriched with public offer labels, save-for-later, validation, cart-driven checkout | operations diagnostics | Cart, Checkout, Catalog, Pricing, Promotion, Inventory, Payment, Order |
| Order / fulfillment | history, customer-readable order detail, shipment tracking, cancellation | pageable order drill-down, contextual fulfillment navigation, shipment state transition | Order, Fulfillment |
| Return / refund | create/list/dispute | approve/reject/receive/inspect/refund with explicit authorization and confirmations | Return, Inventory, Payment |
| Community | review summary/list/create/helpful, comment/reply/react/report | Review read/triage; Comment hide/unhide through `COMMENT_MODERATE` private commands | Review, Comment |
| Notification / account | durable list, mark read, preferences, realtime stream, authorization-backed account UX | — | Notification, Authorization |
| Payment / settlement | order/checkout outcome | payment triage without blind retry; seller-scoped settlement approval | Payment, Settlement |
| Governance | — | role/seller-scope authorization, audit, incidents/recovery | Authorization, Audit, Operations |

## Feature 015 — Admin Console Core Flow

The Feature 015 Admin console adds one permission-aware information architecture and a real-service Playwright flow in `playwright/admin-core-flow.spec.ts`.

Verified flow coverage is intentionally representative rather than pretending every Admin button is an end-to-end test:

- seller operator login uses the real Keycloak `admin-console` client and authorization snapshot;
- permission-aware navigation hides Security/Moderation from a seller operator that lacks those permissions;
- the seller operator creates a real Catalog draft, searches it, confirms publish and observes `PUBLISHED`;
- an authenticated seller navigating directly to Security receives the explicit `/forbidden` state;
- a platform admin hides a real seeded Comment through `/private/comments/{threadId}/hide`, captures the hidden state, then restores it through `/private/comments/{threadId}/unhide`;
- a real seeded Order is opened and its order number is carried into the Fulfillment route;
- the mobile shell opens permission-aware navigation at 390×844 without horizontal overflow.

Screenshots and Playwright traces are produced by `.github/workflows/admin-visual.yml`; the Feature 015 report records the exact successful workflow run and final commit SHA.

## Public sellable-offer contract

Feature 014 closes the former product-detail SKU gap with customer-safe Catalog projections:

- `GET /products/{productId}/offers` lists active SKU choices for a published product.
- `GET /products/offers/{skuId}` resolves one active published SKU for cart/checkout presentation enrichment.
- Catalog owns SKU identity, seller linkage, variant label and purchase-limit metadata.
- Pricing still owns effective monetary calculation; Inventory still owns availability/reservation truth.
- Storefront pages do not call Catalog `/internal/**` endpoints.

## Remaining deliberate API gaps

- Public Catalog does not yet expose customer-friendly category names/tree metadata, so category shortcuts use stable category identifiers rather than inventing browser-owned category data.
- Review moderation remains read/triage only in Feature 015 because the Review bounded context does not yet expose a moderator mutation contract. The Admin console does not invent one.

These gaps are architectural contract gaps, not reasons to bypass service boundaries from Angular.
