# Frontend ↔ backend coverage matrix

The frontend is intentionally split into transport (`ApiService`), bounded-context facade (`MarketplaceApiService`) and routed feature components. Browser code never calls `/internal/**`.

| Business flow | Storefront | Admin/operator | Backend contexts exercised |
| --- | --- | --- | --- |
| Discovery / product 360 | home, category, search cursor, product detail, public sellable offers, price, media, review/comment | catalog, media, pricing | Search, Catalog, Pricing, Media, Review, Comment |
| Seller relationship | follow seller, public shop route when `shopId` is available | seller shop management | Seller, Notification |
| Cart / checkout | versioned cart enriched with public offer labels, save-for-later, validation, cart-driven checkout | operations diagnostics | Cart, Checkout, Catalog, Pricing, Promotion, Inventory, Payment, Order |
| Order / fulfillment | history, customer-readable order detail, shipment tracking, cancellation | order diagnostics, shipment state transition | Order, Fulfillment |
| Return / refund | create/list/dispute | approve/reject/receive/inspect/refund | Return, Inventory, Payment |
| Community | review summary/list/create/helpful, comment/reply/react/report | moderation triage | Review, Comment |
| Notification / account | durable list, mark read, preferences, realtime stream, authorization-backed account UX | — | Notification, Authorization |
| Payment / settlement | order/checkout outcome | payments and seller settlement operations | Payment, Settlement |
| Governance | — | authorization, audit, incidents/recovery | Authorization, Audit, Operations |

## Public sellable-offer contract

Feature 014 closes the former product-detail SKU gap with customer-safe Catalog projections:

- `GET /products/{productId}/offers` lists active SKU choices for a published product.
- `GET /products/offers/{skuId}` resolves one active published SKU for cart/checkout presentation enrichment.
- Catalog owns SKU identity, seller linkage, variant label and purchase-limit metadata.
- Pricing still owns effective monetary calculation; Inventory still owns availability/reservation truth.
- Storefront pages do not call Catalog `/internal/**` endpoints.

## Remaining deliberate API gaps

- Public Catalog does not yet expose customer-friendly category names/tree metadata, so category shortcuts use stable category identifiers rather than inventing browser-owned category data.
- Moderation mutations that are internal-only are not exposed to the browser. Add permission-protected private/admin APIs before wiring them into the console.

These gaps are architectural contract gaps, not reasons to bypass service boundaries from Angular.
