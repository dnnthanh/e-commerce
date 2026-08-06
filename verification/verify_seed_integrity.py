#!/usr/bin/env python3
"""Static integrity gate for production-like seed assets.

Runtime FK/constraint validation still belongs to Docker-backed engine tests. This gate catches
schema drift and deliberately slow/clone-like generators before those tests run.
"""
from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]
failures: list[str] = []

def text(path: str) -> str:
    p = ROOT / path
    if not p.exists():
        failures.append(f"missing {path}")
        return ""
    return p.read_text(encoding="utf-8")

def require(path: str, *tokens: str) -> str:
    value = text(path)
    for token in tokens:
        if token not in value:
            failures.append(f"{path}: missing {token!r}")
    return value

def forbid(path: str, *tokens: str) -> str:
    value = text(path)
    for token in tokens:
        if token in value:
            failures.append(f"{path}: forbidden {token!r}")
    return value

# SQL Server order seeds must match the Feature 013 schema and preserve aggregate money consistency.
require(
    "data/seed-demo/sqlserver/order.sql",
    "marketplace_order(order_no,checkout_key,user_id",
    "seller_order(order_id,seller_id,seller_order_no",
    "order_line(seller_order_id,sku_id,quantity,unit_price,allocated_discount,net_amount)",
    "Reconciliation-safe",
)
large_order = require(
    "data/seed-large/sqlserver/order-large.sql",
    "checkout_key",
    "line_total_reconciles",
    "gross_amount/line_count",
    "payable_amount/line_count",
)
for old_shape in ("order_line(line_id,order_id", "marketplace_order(id,customer_id"):
    if old_shape in large_order:
        failures.append(f"order-large still uses old schema shape {old_shape!r}")

# MySQL large seeds must be set based; million-row procedural loops are intentionally rejected.
forbid("data/seed-large/mysql/review-large.sql", "WHILE i <= 1000000", "CREATE PROCEDURE seed_reviews")
require("data/seed-large/mysql/review-large.sql", "seed_number", "rating_weight", "INSERT IGNORE INTO review")
require("data/seed-large/mysql/seller-large.sql", "seed_number", "seller_tier_weight")

# Rare states must not be shadowed by broader modulo conditions.
checkout_large = text("data/seed-large/postgresql/checkout-large.sql")
unknown_pos = checkout_large.find("THEN 'PAYMENT_UNKNOWN'")
compensated_pos = checkout_large.find("THEN 'COMPENSATED'")
if unknown_pos < 0 or compensated_pos < 0 or unknown_pos > compensated_pos:
    failures.append("checkout-large: PAYMENT_UNKNOWN is missing or shadowed by COMPENSATED")

# Demo data must contain business diversity instead of pure modulo clones.
require(
    "data/seed-demo/mysql/cart.sql",
    "guest_cart",
    "selected_ratio",
    "save_for_later",
    "multi_seller_cart",
)
require(
    "data/seed-demo/postgresql/checkout.sql",
    "COMPENSATED",
    "PAYMENT_UNKNOWN",
    "promotionReserved",
    "inventoryReserved",
    "channel",
)
require(
    "data/seed-demo/postgresql/payment.sql",
    "FAILED",
    "UNKNOWN",
    "PARTIALLY_REFUNDED",
    "payment_reconciliation_state",
)
require(
    "data/seed-demo/sqlserver/fulfillment.sql",
    "GHN",
    "GHTK",
    "VIETTEL_POST",
    "DELIVERY_FAILED",
    "partial_shipment",
)

# Settlement data should exercise line-level and ledger reconciliation, not only header rows.
require(
    "data/seed-large/oracle/settlement-large.sql",
    "settlement_line",
    "seller_settlement_ledger_entry",
    "ledger_reconciliation",
)

# Keep one seed source of truth. Old service-local performance seeds caused schema drift.
legacy_seeds = sorted(ROOT.glob("backend/services/*/src/main/resources/db/changelog/seed/*"))
if legacy_seeds:
    failures.append("legacy service-local seed files remain: " + ", ".join(str(x.relative_to(ROOT)) for x in legacy_seeds))

# The compose seed profile must reference files that really exist.
compose = text("docker-compose.yml")
for seed_ref in re.findall(r"/seed/([A-Za-z0-9_.-]+)", compose):
    candidates = list((ROOT / "data").rglob(seed_ref)) + list((ROOT / "infrastructure").rglob(seed_ref))
    if not candidates:
        failures.append(f"docker-compose references missing seed file {seed_ref}")

if failures:
    print("SEED_INTEGRITY=FAIL")
    for failure in failures:
        print(f"- {failure}")
    raise SystemExit(1)

print("SEED_INTEGRITY=PASS")
print("SCHEMA_DRIFT_STATIC=PASS")
print("SET_BASED_LARGE_SEEDS=PASS")
print("DEMO_BUSINESS_DIVERSITY=PASS")
