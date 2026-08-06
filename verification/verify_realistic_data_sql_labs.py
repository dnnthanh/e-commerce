#!/usr/bin/env python3
"""Static gate for production-like seed distributions and advanced database labs.

This deliberately checks design intent, not database execution. Runtime EXPLAIN evidence is captured
separately when Docker/database engines are available.
"""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
failures: list[str] = []


def require(path: str, *tokens: str) -> str:
    file = ROOT / path
    if not file.exists():
        failures.append(f"missing {path}")
        return ""
    text = file.read_text(encoding="utf-8")
    for token in tokens:
        if token not in text:
            failures.append(f"{path}: missing token {token!r}")
    return text


def forbid(path: str, *tokens: str) -> None:
    file = ROOT / path
    if not file.exists():
        failures.append(f"missing {path}")
        return
    text = file.read_text(encoding="utf-8")
    for token in tokens:
        if token in text:
            failures.append(f"{path}: clone-like token still present {token!r}")

# Data documentation + deterministic distributions.
require(
    "data/README.md",
    "deterministic",
    "80/20",
    "seasonality",
    "long-tail",
    "cross-service",
)

catalog_large = require(
    "data/seed-large/postgresql/catalog-large.sql",
    "seller_rank",
    "category_weight",
    "published_ratio",
    "hot_product",
)
forbid(
    "data/seed-large/postgresql/catalog-large.sql",
    "'Product '||g",
    "'Synthetic realistic catalog item '",
)

require(
    "data/seed-large/postgresql/pricing-large.sql",
    "category_price_band",
    "price_history_depth",
    "scheduled_price",
)
require(
    "data/seed-large/postgresql/inventory-large.sql",
    "warehouse_weight",
    "hot_sku",
    "stockout_ratio",
    "inventory_ledger",
)
require(
    "data/seed-large/postgresql/payment-large.sql",
    "success_ratio",
    "unknown_ratio",
    "provider_weight",
    "business_hour_weight",
)
require(
    "data/seed-large/postgresql/checkout-large.sql",
    "abandon_ratio",
    "retryable_ratio",
    "peak_hour_weight",
)
require(
    "data/seed-large/mysql/review-large.sql",
    "rating_weight",
    "verified_ratio",
    "review_recency_weight",
)
require(
    "data/seed-large/sqlserver/order-large.sql",
    "customer_frequency_weight",
    "seller_popularity_weight",
    "seasonality_weight",
    "multi_seller_ratio",
)
require(
    "data/seed-large/oracle/settlement-large.sql",
    "seller_tier_weight",
    "refund_adjustment_ratio",
)

require(
    "data/seed-large/mysql/seller-large.sql",
    "seller_tier_weight",
    "seller_staff",
    "seller_profile_history",
)
require(
    "data/seed-large/mysql/cart-large.sql",
    "cart_size_weight",
    "selected_ratio",
    "guest_ratio",
)
require(
    "data/seed-large/mongodb/comment-large.js",
    "hotProductRatio",
    "moderationRatio",
    "contentTemplates",
)
require(
    "data/seed-large/mongodb/notification-large.js",
    "unreadRatio",
    "eventWeight",
    "messageTemplates",
)
require(
    "infrastructure/opensearch/seed-demo.sh",
    "_bulk",
    "reviewCount",
    "marketplace-products",
)
require(
    "infrastructure/opensearch/seed-large.sh",
    "_bulk",
    "HOT_PRODUCT_RATIO",
    "marketplace-products",
)

# PostgreSQL labs: queue, complex joins, keyset, JSONB, functional/covering/partial/BRIN,
# extended statistics and partition pruning.
for path, tokens in {
    "database-labs/postgresql/01-baseline/catalog-product-360.sql": (
        "EXPLAIN (ANALYZE, BUFFERS, WAL, SETTINGS)", "WITH", "JOIN", "LATERAL"
    ),
    "database-labs/postgresql/01-baseline/catalog-facet-jsonb.sql": (
        "jsonb", "GROUP BY", "EXPLAIN"
    ),
    "database-labs/postgresql/01-baseline/inventory-ledger-range.sql": (
        "inventory_ledger", "created_at", "EXPLAIN"
    ),
    "database-labs/postgresql/01-baseline/keyset-vs-offset.sql": (
        "OFFSET", "keyset", "ORDER BY"
    ),
    "database-labs/postgresql/02-index/index-toolbox.sql": (
        "INCLUDE", "WHERE status = 'PENDING'", "lower(", "USING gin", "USING brin"
    ),
    "database-labs/postgresql/02-index/extended-statistics.sql": (
        "CREATE STATISTICS", "dependencies", "mcv"
    ),
    "database-labs/postgresql/03-partition/inventory-ledger-native-range.sql": (
        "PARTITION BY RANGE", "PARTITION OF", "DEFAULT"
    ),
    "database-labs/postgresql/03-partition/partition-pruning-lab.sql": (
        "enable_partition_pruning", "EXPLAIN", "created_at"
    ),
    "database-labs/postgresql/05-advanced/skip-locked-queue.sql": (
        "FOR UPDATE SKIP LOCKED", "UPDATE", "RETURNING"
    ),
    "database-labs/postgresql/05-advanced/window-and-lateral.sql": (
        "row_number()", "LATERAL", "FILTER (WHERE"
    ),
}.items():
    require(path, *tokens)

# MySQL labs.
for path, tokens in {
    "database-labs/mysql/02-index/review-index-toolbox.sql": (
        "CREATE INDEX", "product_id", "created_at"
    ),
    "database-labs/mysql/03-partition/review-monthly-range.sql": (
        "PARTITION BY RANGE COLUMNS", "PARTITION pmax"
    ),
    "database-labs/mysql/05-advanced/window-review-ranking.sql": (
        "ROW_NUMBER() OVER", "DENSE_RANK() OVER", "EXPLAIN ANALYZE"
    ),
}.items():
    require(path, *tokens)

# SQL Server labs.
for path, tokens in {
    "database-labs/sqlserver/02-index/order-index-toolbox.sql": (
        "INCLUDE", "WHERE status", "CREATE INDEX"
    ),
    "database-labs/sqlserver/03-partition/order-monthly-partition.sql": (
        "PARTITION FUNCTION", "PARTITION SCHEME"
    ),
    "database-labs/sqlserver/05-advanced/order-window-apply.sql": (
        "CROSS APPLY", "ROW_NUMBER()", "SET STATISTICS IO ON"
    ),
}.items():
    require(path, *tokens)

# Oracle labs.
for path, tokens in {
    "database-labs/oracle/02-index/settlement-index-toolbox.sql": (
        "CREATE INDEX", "UPPER", "seller_id"
    ),
    "database-labs/oracle/03-partition/settlement-interval-partition.sql": (
        "PARTITION BY RANGE", "INTERVAL", "LOCAL"
    ),
    "database-labs/oracle/05-advanced/settlement-analytics.sql": (
        "ROW_NUMBER() OVER", "SUM(", "DBMS_XPLAN.DISPLAY_CURSOR"
    ),
}.items():
    require(path, *tokens)

require(
    "data/CROSS-SERVICE-SCENARIOS.md",
    "PAYMENT_UNKNOWN",
    "PARTIALLY_REFUNDED",
    "eventual-consistency drift",
    "partial shipment",
)

for path, tokens in {
    "database-labs/postgresql/05-advanced/promotion-overlap-gist.sql": (
        "USING gist", "tsrange", "&&", "EXPLAIN"
    ),
    "database-labs/postgresql/06-case-studies/inventory-reconciliation.sql": (
        "FULL OUTER JOIN", "inventory_ledger", "inventory_reservation", "EXPLAIN"
    ),
    "database-labs/sqlserver/06-case-studies/order-money-reconciliation.sql": (
        "seller_order", "order_line", "HAVING", "SET STATISTICS IO ON"
    ),
    "database-labs/oracle/06-case-studies/settlement-ledger-reconciliation.sql": (
        "seller_settlement_ledger_entry", "settlement_line", "HAVING", "DBMS_XPLAN.DISPLAY_CURSOR"
    ),
}.items():
    require(path, *tokens)

require(
    "database-labs/LEARNING-GUIDE.md",
    "selectivity",
    "cardinality",
    "covering index",
    "partition pruning",
    "index-only",
    "correlation",
    "write amplification",
    "EXPLAIN",
)

if failures:
    print("REALISTIC_DATA_SQL_LABS=FAIL")
    for failure in failures:
        print(f"- {failure}")
    raise SystemExit(1)

print("REALISTIC_DATA_SQL_LABS=PASS")
print("POSTGRES_ADVANCED_LABS=PASS")
print("MYSQL_ADVANCED_LABS=PASS")
print("SQLSERVER_ADVANCED_LABS=PASS")
print("ORACLE_ADVANCED_LABS=PASS")
