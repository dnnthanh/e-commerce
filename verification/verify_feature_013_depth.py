from pathlib import Path
import re
import sys

ROOT = Path(__file__).resolve().parents[1]
failures = []
passes = []

def check(name: str, condition: bool, detail: str = ""):
    (passes if condition else failures).append((name, detail))

pom = (ROOT / "backend/pom.xml").read_text()
check("Java 25 parent target", "<java.version>25</java.version>" in pom)
check("Spotless configured", "spotless-maven-plugin" in pom)
check("JaCoCo configured", "jacoco-maven-plugin" in pom)

use_case = ROOT / "backend/platform/be-platform-starter/src/main/java/com/dnnthanh/marketplace/be/platform/stereotype/UseCase.java"
persistence = ROOT / "backend/platform/be-platform-starter/src/main/java/com/dnnthanh/marketplace/be/platform/stereotype/Persistence.java"
check("@UseCase exists", use_case.exists() and "@Component" in use_case.read_text())
check("@Persistence exists", persistence.exists() and "@Component" in persistence.read_text())

expected = [
    "backend/services/be-catalog-api/src/main/java/com/dnnthanh/marketplace/be/catalog/api/domain/model/AttributeDefinition.java",
    "backend/services/be-media-api/src/main/java/com/dnnthanh/marketplace/be/media/api/domain/model/MediaAsset.java",
    "backend/services/be-search-api/src/main/java/com/dnnthanh/marketplace/be/search/api/domain/model/SearchIndexVersion.java",
    "backend/services/be-pricing-api/src/main/java/com/dnnthanh/marketplace/be/pricing/api/domain/service/EffectivePriceResolver.java",
    "backend/services/be-promotion-api/src/main/java/com/dnnthanh/marketplace/be/promotion/api/domain/service/PromotionEngine.java",
    "backend/services/be-inventory-api/src/main/java/com/dnnthanh/marketplace/be/inventory/api/domain/model/StockLedger.java",
    "backend/services/be-cart-api/src/main/java/com/dnnthanh/marketplace/be/cart/api/domain/model/ShoppingCart.java",
    "backend/services/be-checkout-api/src/main/java/com/dnnthanh/marketplace/be/checkout/api/domain/model/CheckoutProcess.java",
    "backend/services/be-order-api/src/main/java/com/dnnthanh/marketplace/be/order/api/domain/model/MarketplaceOrder.java",
    "backend/services/be-payment-api/src/main/java/com/dnnthanh/marketplace/be/payment/api/domain/model/Payment.java",
    "backend/services/be-fulfillment-api/src/main/java/com/dnnthanh/marketplace/be/fulfillment/api/domain/model/Shipment.java",
    "backend/services/be-return-api/src/main/java/com/dnnthanh/marketplace/be/returns/api/domain/model/ReturnRequest.java",
    "backend/services/be-review-api/src/main/java/com/dnnthanh/marketplace/be/review/api/domain/model/ProductReview.java",
    "backend/services/be-comment-api/src/main/java/com/dnnthanh/marketplace/be/comment/api/domain/model/CommentThread.java",
    "backend/services/be-notification-api/src/main/java/com/dnnthanh/marketplace/be/notification/api/domain/model/NotificationDelivery.java",
    "backend/services/be-seller-api/src/main/java/com/dnnthanh/marketplace/be/seller/api/domain/model/SellerAccount.java",
    "backend/services/be-authorization-api/src/main/java/com/dnnthanh/marketplace/be/authorization/api/domain/model/AuthorizationPolicy.java",
    "backend/services/be-audit-api/src/main/java/com/dnnthanh/marketplace/be/audit/api/domain/model/AuditEvent.java",
    "backend/services/be-settlement-api/src/main/java/com/dnnthanh/marketplace/be/settlement/api/domain/model/SellerSettlementLedger.java",
    "backend/services/be-operations-api/src/main/java/com/dnnthanh/marketplace/be/operations/api/domain/model/OperationalIncident.java",
]
for rel in expected:
    check(f"production-depth file: {Path(rel).name}", (ROOT / rel).exists(), rel)

# New production-depth classes must not regress to manual input trimming or generic business exceptions.
new_markers = ["StockLedger.java", "ShoppingCart.java", "CheckoutProcess.java", "Payment.java",
               "Shipment.java", "ReturnRequest.java", "ProductReview.java", "SellerAccount.java",
               "AttributeDefinition.java", "MediaAsset.java", "PromotionCandidate.java", "PriceRule.java"]
for name in new_markers:
    matches = list((ROOT / "backend/services").rglob(name))
    # Some names may have legacy duplicates; select the newly introduced package by content when possible.
    for path in matches:
        text = path.read_text()
        if name in {"Shipment.java", "ReturnRequest.java", "ProductReview.java", "SellerAccount.java",
                    "AttributeDefinition.java", "MediaAsset.java", "PromotionCandidate.java", "PriceRule.java",
                    "StockLedger.java", "ShoppingCart.java", "CheckoutProcess.java", "Payment.java"}:
            check(f"no manual trim in {path.name}", ".trim()" not in text, str(path))

service_files = list((ROOT / "backend/services").rglob("*.java"))
service_annotation_files = [p for p in service_files if "@Service" in p.read_text(errors="ignore")]
check("no generic @Service in service implementation", not service_annotation_files,
      ", ".join(map(str, service_annotation_files[:5])))

junit_names = [
    "ShipmentTest.java", "EffectivePriceResolverTest.java", "PromotionEngineTest.java", "StockLedgerTest.java",
    "ShoppingCartTest.java", "CheckoutProcessTest.java", "PaymentTest.java", "ReturnRequestTest.java",
    "ProductReviewTest.java", "SellerAccountTest.java", "SearchIndexVersionTest.java",
    "NotificationPreferenceTest.java", "SellerSettlementLedgerTest.java", "SkuCombinationGeneratorTest.java",
    "MediaAssetTest.java", "AuthorizationPolicyTest.java", "AuditEventTest.java", "OperationalIncidentTest.java",
]
for test in junit_names:
    check(f"JUnit source {test}", any((ROOT / "backend/services").rglob(test)))

check("cross-context smoke source", (ROOT / "verification/src/CrossContextWorkflowSmoke.java").exists())
check("production-depth smoke source", (ROOT / "verification/src/ProductionDepthDomainSmoke.java").exists())
check("architecture matrix", (ROOT / "docs/architecture/013-production-depth-matrix.md").exists())

for name, detail in passes:
    print(f"PASS: {name}" + (f" [{detail}]" if detail else ""))
for name, detail in failures:
    print(f"FAIL: {name}" + (f" [{detail}]" if detail else ""))
print(f"Summary: {len(passes)} passed, {len(failures)} failed")
sys.exit(1 if failures else 0)
