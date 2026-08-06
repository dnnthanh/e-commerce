# Marketplace Angular applications

The repository contains two Angular 22 applications: customer `storefront` and operations/admin `admin`. They use the same backend envelope/error/trace contract through `shared/ApiService` and the bounded-context facade `shared/MarketplaceApiService`.

## HTTP contract

- `get<T>()`: unwrap normal `ApiResponse<T>.data`.
- `getPage<T>()`: relational `Pageable` response with `PageMetadata`.
- `getCursor<T>()`: cursor/search-after response with `CursorMetadata`.
- `send<T>()`: authenticated commands.
- `ApiClientError`: retains backend error code, localized message, validation details, HTTP status and `traceId`.
- Browser code must not call `/internal/**`; internal service contracts remain server-to-server.

## Storefront flows

- Catalog home and OpenSearch discovery with cursor pagination.
- Product 360: catalog, media variants, effective price, review summary, comments/Q&A, seller follow, cart add.
- Optimistic-version cart: quantity/select/remove/save-for-later and backend validation.
- Cart-driven checkout: idempotency key, selected lines, promotion codes, payment provider and persistent saga result.
- Order history + detail + seller-order monetary snapshots + shipment tracking + cancellation.
- Return creation and dispute lifecycle.
- Review/comment community actions.
- Durable notification inbox + realtime stream + notification preferences.
- Account authorization snapshot and seller shop view.

The public catalog contract currently does not expose a complete public SKU/offer projection. Product detail therefore requires a SKU identifier for effective pricing/cart actions in the demo. This is documented as a backend API gap rather than inventing a browser call to the internal catalog SKU endpoint.

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
npm install
npm run build:storefront
npm run build:admin
```

Docker uses frontend-scoped build contexts:

```bash
docker build -f frontend/Dockerfile.storefront frontend
docker build -f frontend/Dockerfile.admin frontend
```

Static contract/syntax gates:

```bash
python ci-cdconfigs/verify_frontend_backend_coverage_v11.py
node ci-cdconfigs/verify_frontend_typescript_syntax_v11.js
```

See [`COVERAGE-MATRIX.md`](COVERAGE-MATRIX.md) for the storefront/admin bounded-context coverage and explicit API gaps.
