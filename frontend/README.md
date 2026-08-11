# Marketplace Angular applications

The repository contains two Angular 22 applications: customer `storefront` and operations/admin `admin`. They share the backend envelope/error/trace contract through `shared/ApiService` and the bounded-context facade `shared/MarketplaceApiService`.

## HTTP contract

- `get<T>()`: unwrap normal `ApiResponse<T>.data`.
- `getPage<T>()`: relational `Pageable` response with `PageMetadata`.
- `getCursor<T>()`: cursor/search-after response with `CursorMetadata`.
- `send<T>()`: authenticated commands.
- `ApiClientError`: retains backend error code, localized message, validation details, HTTP status and `traceId`.
- Browser code must not call `/internal/**`; internal service contracts remain server-to-server.

## Storefront core customer flow

The storefront is routed as a customer journey instead of an API diagnostic shell:

```text
Home / Category / Search
    -> Product Detail + public Catalog offers
    -> Cart
    -> Checkout
    -> Order Detail / Tracking
```

Main routes:

- `/` — Catalog-backed marketplace home.
- `/search` — OpenSearch discovery with cursor/search-after pagination and route query state.
- `/category/:id` — public Catalog category filtering.
- `/product/:id` — Product 360 with media/community plus real sellable variant selection.
- `/cart` — authenticated versioned cart enriched with public Catalog offer labels.
- `/checkout` — authenticated cart-driven, idempotent persistent-Saga checkout.
- `/orders` and `/orders/:orderNo` — customer order history, monetary summary and shipment tracking.
- `/account` — authorization-aware account UX and notification preferences.

### Public Catalog offer projection

Product Detail no longer asks the user to type an SKU identifier. The storefront uses:

```http
GET /products/{productId}/offers
GET /products/offers/{skuId}
```

The projection exposes only customer-safe Catalog data such as SKU identity, product/seller linkage, variant label and purchase limit. Effective price still comes from Pricing and inventory/reservation truth still belongs to Inventory.

The single-SKU lookup is also used to enrich existing cart lines after page reload; the cart remains authoritative for version, quantity and price snapshot.

## Admin/operations flows

- Seller shop management.
- Catalog create/publish lifecycle.
- Media upload session and generated variant diagnostics.
- Effective pricing diagnostics.
- Promotion evaluation workbench (reservation APIs stay internal).
- Inventory pageable balance/version view.
- Order 360 and shipment view.
- Fulfillment state transitions through the domain state machine.
- Payment status/UNKNOWN monitoring.
- Return seller action, receiving, inspection and refund operations.
- Review/comment triage without exposing internal moderation endpoints to browser code.
- Settlement approval.
- Security roles/seller scopes, audit history and recovery incidents.

## Production build

```bash
cd frontend
npm install --no-audit --no-fund
npm run build:storefront
npm run build:admin
```

Docker uses frontend-scoped build contexts:

```bash
docker build -f frontend/Dockerfile.storefront frontend
docker build -f frontend/Dockerfile.admin frontend
```

Repository static gates include:

```bash
python verification/verify_storefront_core_flow_v14.py
python verification/verify_keycloak_authority_v17.py
for script in verification/verify_*.py; do python "$script"; done
```

See [`COVERAGE-MATRIX.md`](COVERAGE-MATRIX.md) for bounded-context coverage and remaining deliberate API gaps.
