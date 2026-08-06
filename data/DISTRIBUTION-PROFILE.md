# Seed distribution profile for query-planner labs

The large seed uses deterministic formulas instead of uniform random/modulo clones. The percentages
below are properties of the generators and are intentionally stable between reloads. They are useful
when you practice **selectivity**, cardinality estimation and index design.

| Dataset | Distribution | Why it matters to the planner |
| --- | --- | --- |
| Catalog status | 93% PUBLISHED, 3% DRAFT, 2.5% SUSPENDED, 1.5% ARCHIVED | `status='PUBLISHED'` alone is low-selectivity; combine with seller/category/time instead of expecting a magic status index |
| Catalog seller cohort | top 20 sellers ≈55%, next 80 ≈30%, long-tail 400 ≈15% | seller cardinality is heavily skewed; MCV/extended statistics become useful |
| Catalog hot products | 8% hot cohort | lets search/order/review data concentrate on a small product set |
| Catalog recency | 60% within 90 days, 40% historical tail | useful for time range and BRIN/B-tree comparisons |
| Catalog outbox | 0.5% PENDING, 0.3% FAILED, 99.2% PROCESSED | classic partial-index case: `WHERE status='PENDING'` stays tiny |
| Inventory stock | 2% stockout, 5% very low, 13% medium, 80% healthy | supports low-stock/oversell queries without making every SKU identical |
| Inventory hot SKU | 8%, also stocked at an extra major warehouse | creates multi-warehouse skew and hot-key contention |
| Payment | ≈95.30% PAID, 2.06% PENDING, 1.26% FAILED, 0.80% PARTIALLY_REFUNDED, 0.40% REFUNDED, 0.18% UNKNOWN | rare-state queries can justify partial indexes/reconciliation queues |
| Checkout | ≈87.50% COMPLETED, 5.80% PAYMENT_PENDING, 5.38% COMPENSATED, 1.20% FAILED_RETRYABLE, 0.12% PAYMENT_UNKNOWN | gives recovery/operations queries realistic rarity |
| Review rating | 3% ★1, 5% ★2, 17% ★3, 37% ★4, 38% ★5 | 4–5 star bias is closer to real verified-review behavior than uniform ratings |
| Marketplace order status | 2% CANCELLED, 3.5% CREATED, 6% PAYMENT_PENDING, 27.5% PAID, 33% FULFILLING, 28% COMPLETED | supports status/time/seller query-plan comparisons |
| Seller count per order | 72% one seller, 23% two sellers, 5% three sellers | exercises parent/seller-order joins and partial fulfillment |
| Seller cohort | top 20 ≈55% of generated references, next 80 ≈30%, long-tail 400 ≈15% | intentionally creates hot sellers and long tail |

## How to use this profile

Before adding an index, predict the expected selectivity. For example, a partial outbox index covering
only ~0.5% PENDING rows is very different from an index whose first column is Catalog `status` where
93% of rows are PUBLISHED. Then run `EXPLAIN (ANALYZE, BUFFERS)` and compare your prediction with
`actual rows`, pages/buffers touched and total execution time.

The profile is a **generator contract**, not benchmark output. Real execution plans and measured
latency must be captured from the database engine after loading `seed-large`; they are never invented
from these percentages.
