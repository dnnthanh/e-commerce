#!/usr/bin/env python3
from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]
JAVA = list((ROOT / "backend/services").rglob("src/main/java/**/*.java"))
texts = {p: p.read_text(encoding="utf-8", errors="ignore") for p in JAVA}
required = [
    "StockLedgerPort",
    "CheckoutProcessPort",
    "ProductReviewPort",
    "SettlementLedgerPort",
    "ProductSearchPort",
    "ShipmentRepositoryPort",
    "SellerAccountPort",
    "CartPersistencePort",
    "MediaAssetPort",
    "PaymentRepositoryPort",
    "NotificationInboxPort",
    "PromotionUsagePort",
    "ReturnRepositoryPort",
]
failed = []
for port in required:
    implementations = [p for p, text in texts.items() if re.search(r"\bimplements\b[^\{]*\b" + re.escape(port) + r"\b", text, re.DOTALL)]
    if implementations:
        print(f"PASS: {port} is backed by a concrete adapter")
    else:
        print(f"FAIL: {port} is still dormant (no adapter implementation)")
        failed.append(port)

conditional = []
for p, text in texts.items():
    if "@ConditionalOnBean" in text and any(port in text for port in required):
        conditional.append(p)
if conditional:
    print("FAIL: production-depth use cases are still conditionally disabled:")
    for p in conditional:
        print(f"  - {p.relative_to(ROOT)}")
    failed.append("conditional-usecases")
else:
    print("PASS: production-depth use cases are not conditionally disabled")

print(f"Summary: {len(required) + 1 - len(set(failed))} passed, {len(set(failed))} failed")
raise SystemExit(1 if failed else 0)
