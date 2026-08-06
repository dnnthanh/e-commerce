# Frontend ↔ backend coverage matrix

The frontend is intentionally split into transport (`ApiService`), bounded-context facade (`MarketplaceApiService`) and route components. Browser code never calls `/internal/**`.

| Business flow | Storefront | Admin/operator | Backend contexts exercised |
| --- | --- | --- | --- |
| Discovery / product 360 | search cursor, product detail, price, media, review/comment | catalog, media, pricing | Search, Catalog, Pricing, Media, Review, Comment |
| Seller relationship | follow seller, public shop route when `shopId` is available | seller shop management | Seller, Notification |
| Cart / checkout | versioned cart, save-for-later, validation, cart-driven checkout | operations diagnostics | Cart, Checkout, Pricing, Promotion, Inventory, Payment, Order |
| Order / fulfillment | history, order detail, shipment tracking, cancellation | order diagnostics, shipment state transition | Order, Fulfillment |
| Return / refund | create/list/dispute | approve/reject/receive/inspect/refund | Return, Inventory, Payment |
| Community | review summary/list/create/helpful, comment/reply/react/report | moderation triage | Review, Comment |
| Notification / account | durable list, mark read, preferences, realtime stream, auth snapshot | — | Notification, Authorization |
| Payment / settlement | order/checkout outcome | payments and seller settlement operations | Payment, Settlement |
| Governance | — | authorization, audit, incidents/recovery | Authorization, Audit, Operations |

## Deliberate API gaps

- The public Catalog product response does not yet provide a complete sellable SKU/offer projection. The UI therefore does not call an internal SKU-owner endpoint. Product detail keeps the SKU diagnostic input visible until a permission-safe public offer projection exists.
- Moderation mutations that are internal-only are not exposed to the browser. Add permission-protected private/admin APIs before wiring them into the console.

These gaps are architectural contract gaps, not reasons to bypass service boundaries from Angular.
