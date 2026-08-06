# Marketplace seed data

This directory contains **deterministic synthetic data designed to behave like production data**.
It is intentionally not a clone of any real customer or retailer dataset.

## Why this dataset is different from `g % N` demo data

Performance plans become misleading when every seller, category, SKU, customer and timestamp has the
same frequency. Real marketplaces are skewed:

- **80/20 seller distribution**: a small seller cohort generates most orders/revenue while a long
  tail sells occasionally.
- **Long-tail products/SKUs**: a small hot set receives many orders/reviews/reservations; most SKUs
  are cold.
- **Category weights**: phone/laptop/accessory traffic is not uniformly distributed.
- **Seasonality**: campaign periods, weekends and recent months receive more traffic.
- **Business-hour peaks**: checkout/payment timestamps cluster around lunch/evening rather than every
  hour being equally likely.
- **Rare failure states**: UNKNOWN payments, retryable checkout failures, stockouts and suspended
  sellers are deliberately uncommon but present.
- **Review bias**: verified reviews skew toward 4–5 stars with a small 1–2 star tail.
- **Warehouse skew**: HCM/Hanoi hubs hold more inventory and hot SKUs are stocked in multiple
  warehouses.
- **Cross-service identifiers**: large catalog product/SKU ranges are shared conceptually by pricing,
  inventory, order, review and search seed logic.

The distribution intentionally models seasonality, long-tail demand and cross-service identifier consistency.

All generation is deterministic: rerunning against an empty database produces the same statistical
shape. That makes `EXPLAIN (ANALYZE, BUFFERS)` comparisons reproducible.

See [`DISTRIBUTION-PROFILE.md`](DISTRIBUTION-PROFILE.md) for the expected selectivity of the major
cohorts and [`CROSS-SERVICE-SCENARIOS.md`](CROSS-SERVICE-SCENARIOS.md) for the readable demo flows.

## ID ranges

| Entity | Demo range | Large/performance range |
| --- | --- | --- |
| Product | 1001–1120 | 1,000,001–1,500,000 |
| SKU | 2001–2240 | 2,000,001–3,000,000 |
| Seller | 10001–10012 | 10001–10500 |
| Customer | `demo-customer-*` | `customer-*` |
| Order | `ORD-DEMO-*` | `ORD-L-*` |
| Payment | `PAY-DEMO-*` | `PAY-L-*` |

## Profiles

### `seed-demo`

Small enough for normal local startup. It favors readability and visible edge cases:

- published + draft/suspended catalog rows;
- category-specific product names and SKU variants;
- stockout/low-stock/healthy inventory examples;
- payment PAID/PENDING/UNKNOWN/FAILED/refund examples;
- reviews with varied titles/content/rating;
- order/fulfillment lifecycle diversity.

### `seed-large`

For database and performance labs only. It intentionally creates hundreds of thousands or millions of
rows with realistic skew.

Do **not** load it on every application startup.

## Performance-lab workflow

1. Start databases and run Liquibase schema migrations.
2. Load `seed-large`.
3. Run a baseline query under `database-labs/<engine>/01-baseline`.
4. Save the real execution plan.
5. Apply **one** candidate index/statistics change from `02-index`.
6. Rerun the exact query and compare.
7. Only then test partitioning from `03-partition`.
8. Reset/reload before comparing another candidate.

The lab rule is: **measure first, change one variable, measure again**. Indexes and partitions are not
automatically improvements; they trade read cost against memory, storage, write amplification and
maintenance complexity.
