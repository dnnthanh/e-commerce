#!/usr/bin/env python3
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
BACKEND = ROOT / "backend"
failures = []

def require(condition, message):
    if not condition:
        failures.append(message)

def text(path):
    p = BACKEND / path
    return p.read_text(encoding="utf-8") if p.exists() else ""

# Package shape / typed error model.
require((BACKEND / "platform/be-platform-starter/src/main/java/com/dnnthanh/marketplace/be/platform/exception/ErrorCode.java").exists(), "ErrorCode contract missing")
require((BACKEND / "platform/be-platform-starter/src/main/java/com/dnnthanh/marketplace/be/platform/exception/PlatformErrorCode.java").exists(), "PlatformErrorCode enum missing")
require((BACKEND / "platform/be-platform-starter/src/main/java/com/dnnthanh/marketplace/be/platform/i18n/MessageResolver.java").exists(), "MessageResolver contract missing")
require((BACKEND / "platform/be-platform-starter/src/main/java/com/dnnthanh/marketplace/be/platform/i18n/SpringMessageResolver.java").exists(), "SpringMessageResolver missing")
require((BACKEND / "platform/be-platform-starter/src/main/java/com/dnnthanh/marketplace/be/platform/model/CodeEnum.java").exists(), "CodeEnum missing")

business = text("platform/be-platform-starter/src/main/java/com/dnnthanh/marketplace/be/platform/exception/BusinessException.java")
require("@Getter" in business, "BusinessException should use Lombok @Getter")
require("ErrorCode errorCode" in business, "BusinessException should carry typed ErrorCode")
require("String code" not in business and "HttpStatus httpStatus" not in business, "BusinessException must not carry hard-coded code/status fields")
service_token_exception = text("platform/be-platform-starter/src/main/java/com/dnnthanh/marketplace/be/platform/exception/ServiceTokenAcquisitionException.java")
require("extends BusinessException" in service_token_exception, "ServiceTokenAcquisitionException must use the typed BusinessException path")
require("SERVICE_TOKEN_ACQUISITION_FAILED" in service_token_exception, "ServiceTokenAcquisitionException must use PlatformErrorCode")

handler = text("platform/be-platform-starter/src/main/java/com/dnnthanh/marketplace/be/platform/web/error/GlobalExceptionHandler.java")
require("PlatformErrorCode." in handler, "GlobalExceptionHandler must use PlatformErrorCode")
for literal in ['"VALIDATION_FAILED"', '"BAD_REQUEST"', '"UNAUTHORIZED"', '"FORBIDDEN"', '"INTERNAL_ERROR"']:
    require(literal not in handler, f"GlobalExceptionHandler still hard-codes {literal}")
require("MessageResolver" in handler, "GlobalExceptionHandler must resolve messages through MessageResolver")
require("@RequiredArgsConstructor" in handler, "GlobalExceptionHandler should use Lombok constructor generation")

# Comment mapper: enums map as enums; no hand-written enum String conversion.
mapper = text("services/be-comment-api/src/main/java/com/dnnthanh/marketplace/be/comment/api/adapter/in/web/mapper/CommentApiMapper.java")
require(".name()" not in mapper, "CommentApiMapper must not convert enums with name()")
require('expression = "java(' not in mapper, "CommentApiMapper should avoid java(expression) for current mappings")
thread_response = text("services/be-comment-api/src/main/java/com/dnnthanh/marketplace/be/comment/api/api/response/CommentThreadResponse.java")
reaction_response = text("services/be-comment-api/src/main/java/com/dnnthanh/marketplace/be/comment/api/api/response/CommentReactionResponse.java")
report_response = text("services/be-comment-api/src/main/java/com/dnnthanh/marketplace/be/comment/api/api/response/CommentReportResponse.java")
require("CommentStatus status" in thread_response, "CommentThreadResponse.status must be CommentStatus")
require("ReactionType type" in reaction_response, "CommentReactionResponse.type must be ReactionType")
require("CommentReportReason reason" in report_response, "CommentReportResponse.reason must be CommentReportReason")
for enum_path in [
    "services/be-comment-api/src/main/java/com/dnnthanh/marketplace/be/comment/api/domain/enumtype/CommentStatus.java",
    "services/be-comment-api/src/main/java/com/dnnthanh/marketplace/be/comment/api/domain/enumtype/ReactionType.java",
    "services/be-comment-api/src/main/java/com/dnnthanh/marketplace/be/comment/api/domain/enumtype/CommentReportReason.java",
]:
    enum_text = text(enum_path)
    require("implements I18nCodeEnum" in enum_text, f"{enum_path} must implement I18nCodeEnum")
    require("@JsonValue" in enum_text and "String code" in enum_text, f"{enum_path} must expose a stable @JsonValue code")


comment_config = text("services/be-comment-api/src/main/resources/application.yml")
require("basename: messages,i18n/comment-messages" in comment_config, "Comment service must register its business i18n bundle")
for bundle in [
    "services/be-comment-api/src/main/resources/i18n/comment-messages.properties",
    "services/be-comment-api/src/main/resources/i18n/comment-messages_vi.properties",
]:
    bundle_text = text(bundle)
    require("enum.CommentStatus.PUBLISHED=" in bundle_text, f"{bundle} missing CommentStatus labels")
    require("enum.ReactionType.LIKE=" in bundle_text, f"{bundle} missing ReactionType labels")
    require("enum.CommentReportReason.SPAM=" in bundle_text, f"{bundle} missing CommentReportReason labels")

# Every service enum (including nested enums) must expose a stable CodeEnum contract.
for p in BACKEND.glob("services/**/src/main/java/**/*.java"):
    s = p.read_text(encoding="utf-8")
    if " enum " not in s and "public enum " not in s:
        continue
    import re
    for match in re.finditer(r"(?:public\s+)?enum\s+(\w+)\s*([^\{]*)\{", s):
        declaration_tail = match.group(2)
        require(
            "implements CodeEnum" in declaration_tail
            or "implements I18nCodeEnum" in declaration_tail
            or "implements ErrorCode" in declaration_tail,
            f"{p.relative_to(BACKEND)} enum {match.group(1)} must expose CodeEnum/ErrorCode",
        )

# Closed-set API states remain typed as enums; transport mapping must not stringify them.
typed_api_contracts = {
    "services/be-checkout-api/src/main/java/com/dnnthanh/marketplace/be/checkout/api/api/response/CheckoutResponse.java": ["CheckoutSaga.State status", "CheckoutPaymentStatus paymentStatus"],
    "services/be-inventory-api/src/main/java/com/dnnthanh/marketplace/be/inventory/api/api/response/ReservationResponse.java": ["ReservationStatus status"],
    "services/be-inventory-api/src/main/java/com/dnnthanh/marketplace/be/inventory/api/api/response/OrderReservationView.java": ["ReservationStatus status"],
    "services/be-fulfillment-api/src/main/java/com/dnnthanh/marketplace/be/fulfillment/api/api/response/ShipmentView.java": ["ShipmentStatus status"],
    "services/be-return-api/src/main/java/com/dnnthanh/marketplace/be/returns/api/api/response/ReturnView.java": ["ReturnStatus status"],
    "services/be-seller-api/src/main/java/com/dnnthanh/marketplace/be/seller/api/api/response/ShopView.java": ["SellerStatus status"],
    "services/be-settlement-api/src/main/java/com/dnnthanh/marketplace/be/settlement/api/api/response/SettlementView.java": ["SettlementStatus status"],
}
for contract, expected_types in typed_api_contracts.items():
    contract_text = text(contract)
    for expected_type in expected_types:
        require(expected_type in contract_text, f"{contract} must keep {expected_type} typed as enum")

for mapper_path in [
    "services/be-comment-api/src/main/java/com/dnnthanh/marketplace/be/comment/api/adapter/in/web/mapper/CommentApiMapper.java",
    "services/be-seller-api/src/main/java/com/dnnthanh/marketplace/be/seller/api/adapter/in/web/mapper/SellerApiMapper.java",
    "services/be-checkout-api/src/main/java/com/dnnthanh/marketplace/be/checkout/api/adapter/in/web/mapper/CheckoutApiMapper.java",
]:
    mapper_text = text(mapper_path)
    require(".name()" not in mapper_text, f"{mapper_path} must not stringify enums")

# Existing BusinessException subclasses must be enum-driven rather than tuples.
for p in BACKEND.glob("services/**/src/main/java/**/*Exception.java"):
    s = p.read_text(encoding="utf-8")
    if "extends BusinessException" in s:
        require("HttpStatus" not in s, f"{p.relative_to(BACKEND)} must not hard-code HttpStatus")
        require("super(\"" not in s, f"{p.relative_to(BACKEND)} must use a typed service error enum")

if failures:
    print(f"FAILED: {len(failures)}")
    for failure in failures:
        print(f"- {failure}")
    sys.exit(1)
print("PASSED: platform core i18n/enum contract")
