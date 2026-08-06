# Project implementation preview

Generated from the actual sandbox filesystem at 2026-08-03T05:37:20.596729+00:00.

## Current artifact counts

| Item | Count |
| --- | ---: |
| Backend `be-*` service directories | 53 |
| Backend Java files | 285 |
| Backend Maven POM files | 56 |
| Backend test-source files | 14 |
| Seed files under `data/` | 41 |

## Service source coverage

| Deployable | Java files | DB changelog files |
| --- | ---: | ---: |
| `be-audit-api` | 4 | 2 |
| `be-audit-worker` | 2 | 0 |
| `be-authorization-api` | 9 | 2 |
| `be-authorization-outbox` | 2 | 0 |
| `be-cart-api` | 12 | 2 |
| `be-catalog-api` | 19 | 2 |
| `be-catalog-outbox` | 2 | 0 |
| `be-checkout-api` | 19 | 2 |
| `be-checkout-job` | 3 | 0 |
| `be-comment-api` | 8 | 0 |
| `be-comment-outbox` | 2 | 0 |
| `be-fulfillment-api` | 6 | 2 |
| `be-fulfillment-outbox` | 2 | 0 |
| `be-fulfillment-worker` | 3 | 0 |
| `be-gateway` | 4 | 0 |
| `be-inventory-api` | 15 | 3 |
| `be-inventory-job` | 2 | 0 |
| `be-inventory-outbox` | 2 | 0 |
| `be-inventory-worker` | 3 | 0 |
| `be-media-api` | 6 | 2 |
| `be-media-outbox` | 2 | 0 |
| `be-media-worker` | 3 | 0 |
| `be-notification-api` | 6 | 0 |
| `be-notification-job` | 2 | 0 |
| `be-notification-realtime` | 2 | 0 |
| `be-notification-worker` | 2 | 0 |
| `be-operations-api` | 4 | 2 |
| `be-operations-outbox` | 2 | 0 |
| `be-operations-worker` | 2 | 0 |
| `be-order-api` | 8 | 3 |
| `be-order-outbox` | 2 | 0 |
| `be-order-worker` | 2 | 0 |
| `be-payment-api` | 16 | 2 |
| `be-payment-job` | 3 | 0 |
| `be-payment-outbox` | 2 | 0 |
| `be-payment-simulator` | 3 | 0 |
| `be-payment-worker` | 2 | 0 |
| `be-pricing-api` | 8 | 2 |
| `be-promotion-api` | 8 | 2 |
| `be-return-api` | 6 | 2 |
| `be-return-outbox` | 2 | 0 |
| `be-return-worker` | 2 | 0 |
| `be-review-api` | 9 | 2 |
| `be-review-outbox` | 2 | 0 |
| `be-review-worker` | 4 | 0 |
| `be-search-api` | 5 | 0 |
| `be-search-worker` | 2 | 0 |
| `be-seller-api` | 9 | 2 |
| `be-seller-outbox` | 2 | 0 |
| `be-settlement-api` | 4 | 3 |
| `be-settlement-job` | 2 | 0 |
| `be-settlement-outbox` | 2 | 0 |
| `be-settlement-worker` | 4 | 0 |

## Verification scope

Static architecture/configuration verifiers are stored under `verification/` and their latest combined output is under `.agent/reports/full-marketplace/`. Docker runtime and a full Maven reactor build are separate gates and must not be inferred from static verification.

## V11 depth update

- Storefront now follows customer business flows across catalog/search/pricing/media/cart/checkout/order/fulfillment/returns/reviews/comments/notifications.
- Admin now exposes bounded-context operational screens for catalog, media, pricing, promotion, inventory, orders, fulfillment, payments, returns, moderation triage, sellers, settlements, authorization, audit and recovery.
- Browser code stays on public/private contracts; internal routes remain server-to-server.
- Docker builds are scoped to `backend/` or `frontend/`, and split compose files use repository-root project-directory semantics.
- Database labs include 32 deep incidents across PostgreSQL, MySQL, SQL Server and Oracle with baseline/solution/regression evidence workflows.
