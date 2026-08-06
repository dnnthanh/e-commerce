from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
BE = ROOT / "backend" / "services"
errors = []
passed = 0

def check(condition, message):
    global passed
    if condition:
        passed += 1
    else:
        errors.append(message)

def text(path):
    p = ROOT / path
    if not p.exists():
        errors.append(f"missing file: {path}")
        return ""
    return p.read_text(encoding="utf-8")

notification = text("backend/services/be-notification-api/src/main/java/com/dnnthanh/marketplace/be/notification/api/api/response/NotificationView.java")
check("import java.util.Map;" in notification, "NotificationView must import java.util.Map")

price = text("backend/services/be-pricing-api/src/test/java/com/dnnthanh/marketplace/be/pricing/api/domain/model/PriceRuleTest.java")
check("activeAt(" not in price, "PriceRuleTest still calls removed activeAt")
check(".applies(" in price and "PriceRule.PriceContext" in price, "PriceRuleTest must use applies(PriceContext)")
check("PriceSource." in price, "PriceRuleTest must construct current PriceRule with PriceSource")

promotion = text("backend/services/be-promotion-api/src/test/java/com/dnnthanh/marketplace/be/promotion/api/domain/model/PromotionTest.java")
check("Promotion.PromotionEvaluationContext" in promotion, "PromotionTest must use PromotionEvaluationContext")
check("promotion.eligible(context)" in promotion, "PromotionTest must call current eligible(context)")
check("BigDecimal.ZERO,\n                        0,\n                        0" in promotion, "PromotionTest must provide usage limits")

returns = text("backend/services/be-return-api/src/test/java/com/dnnthanh/marketplace/be/returns/api/domain/model/ReturnRequestTest.java")
check("acceptInspection" not in returns, "ReturnRequestTest still calls removed acceptInspection")
check("ReturnRequest.Status" not in returns, "ReturnRequestTest still references removed nested Status")
check("r::receive" not in returns and "request.receive(99L)" in returns, "ReturnRequestTest must pass receiving warehouse")
check("request.inspect(" in returns and "request.prepareRefund();" in returns, "ReturnRequestTest must exercise inspect -> prepareRefund")
check("ReturnStatus.REFUND_PENDING" in returns, "ReturnRequestTest must use ReturnStatus")
check("new ReturnRequest.ReturnLine(" in returns and "LocalDateTime.of(" in returns, "ReturnRequestTest must provide current ReturnLine snapshot fields")

shop = text("backend/services/be-seller-api/src/test/java/com/dnnthanh/marketplace/be/seller/api/domain/model/ShopTest.java")
check('"ACTIVE"' not in shop and "SellerStatus.ACTIVE" in shop, "ShopTest must use SellerStatus enum")
check("IllegalArgumentException.class" not in shop and "InvalidShopException.class" in shop, "ShopTest must assert InvalidShopException")
check('updateMaterialInfo(" New "' not in shop, "ShopTest must not test transport trimming inside domain model")

# Remaining reactor tests after be-return-api must still target methods/types present in production source.
review_test = text("backend/services/be-review-api/src/test/java/com/dnnthanh/marketplace/be/review/api/domain/model/ProductReviewTest.java")
review_model = text("backend/services/be-review-api/src/main/java/com/dnnthanh/marketplace/be/review/api/domain/model/ProductReview.java")
for token in ["markHelpful", "edit("]:
    check(token in review_test and token in review_model, f"Review test/source drift for {token}")

search_test = text("backend/services/be-search-api/src/test/java/com/dnnthanh/marketplace/be/search/api/domain/model/SearchIndexVersionTest.java")
search_model = text("backend/services/be-search-api/src/main/java/com/dnnthanh/marketplace/be/search/api/domain/model/SearchIndexVersion.java")
check("accept(" in search_test and "accept(" in search_model, "SearchIndexVersionTest/source drift")

seller_test = text("backend/services/be-seller-api/src/test/java/com/dnnthanh/marketplace/be/seller/api/domain/model/SellerAccountTest.java")
seller_model = text("backend/services/be-seller-api/src/main/java/com/dnnthanh/marketplace/be/seller/api/domain/model/SellerAccount.java")
for token in ["submit()", "verify()", "activate()", "suspend()", "reinstate()"]:
    check(token in seller_test and token in seller_model, f"SellerAccountTest/source drift for {token}")
for forbidden in ["addStaff(", "hasPermission(", "permissionsCsv", "permissions_csv"]:
    check(forbidden not in seller_test and forbidden not in seller_model, f"SellerAccount must not own authorization: {forbidden}")

settlement_test = text("backend/services/be-settlement-api/src/test/java/com/dnnthanh/marketplace/be/settlement/api/domain/model/SellerSettlementLedgerTest.java")
settlement_model = text("backend/services/be-settlement-api/src/main/java/com/dnnthanh/marketplace/be/settlement/api/domain/model/SellerSettlementLedger.java")
for token in ["append(", "payableBalance()"]:
    check(token in settlement_test and token in settlement_model, f"Settlement test/source drift for {token}")

for service in [
    "be-return-outbox", "be-return-worker", "be-review-outbox", "be-review-worker",
    "be-search-worker", "be-seller-outbox", "be-settlement-job", "be-settlement-outbox", "be-settlement-worker"
]:
    test_root = BE / service / "src" / "test" / "java"
    java_tests = list(test_root.rglob("*.java")) if test_root.exists() else []
    check(not java_tests, f"{service} unexpectedly contains unreviewed test sources: {java_tests}")

print(f"REMAINING_UNIT_TEST_ALIGNMENT_V10: {passed} passed, {len(errors)} failed")
if errors:
    for error in errors:
        print(f"FAIL: {error}")
    raise SystemExit(1)
print("PASS")
