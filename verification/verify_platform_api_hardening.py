#!/usr/bin/env python3
from pathlib import Path
import re
import sys

ROOT = Path(__file__).resolve().parents[1]
failures: list[str] = []


def text(path: str) -> str:
    p = ROOT / path
    if not p.exists():
        failures.append(f"missing: {path}")
        return ""
    return p.read_text(encoding="utf-8")


def require(condition: bool, message: str) -> None:
    if not condition:
        failures.append(message)

platform = "backend/platform/be-platform-starter/src/main/java/com/dnnthanh/marketplace/be/platform"

# Shared response contract + Java Jackson NON_NULL.
require((ROOT / f"{platform}/api/ApiResponse.java").exists(), "ApiResponse must live in be-platform-starter")
require((ROOT / f"{platform}/api/PageMetadata.java").exists(), "PageMetadata missing")
require((ROOT / f"{platform}/api/CursorMetadata.java").exists(), "CursorMetadata missing")
require((ROOT / f"{platform}/api/ApiResponseBodyAdvice.java").exists(), "ApiResponseBodyAdvice missing")
jackson = text(f"{platform}/jackson/PlatformJacksonConfiguration.java")
require("changeDefaultPropertyInclusion" in jackson, "Jackson NON_NULL must be configured via Java builder")
require("JsonInclude.Include.NON_NULL" in jackson, "Jackson Java config must use NON_NULL")

# Trace must be observability context, not user identity context.
user_context = text(f"{platform}/context/UserContext.java")
require("traceId" not in user_context, "UserContext must not contain traceId")
trace_headers = text(f"{platform}/trace/TraceHeaders.java")
require('"trace-id"' in trace_headers, "trace-id header constant missing")
logging_filter = text(f"{platform}/web/logging/StructuredHttpLoggingFilter.java")
require("TraceHeaders.TRACE_ID" in logging_filter and "setHeader" in logging_filter,
        "HTTP response must expose trace-id header")
trace_config = text(f"{platform}/trace/TracePropagationConfiguration.java")
require("RestClientCustomizer" in trace_config and "requestInterceptor" in trace_config,
        "RestClient trace-id propagation missing")

# Exception coverage.
handler = text(f"{platform}/web/error/GlobalExceptionHandler.java")
for token in [
    "HandlerMethodValidationException",
    "ConstraintViolationException",
    "HttpMessageNotReadableException",
    "MethodArgumentTypeMismatchException",
    "NoResourceFoundException",
    "HttpRequestMethodNotSupportedException",
    "HttpMediaTypeNotSupportedException",
    "DataIntegrityViolationException",
    "Exception.class",
]:
    require(token in handler, f"GlobalExceptionHandler missing {token}")
require("ApiResponse" in handler, "GlobalExceptionHandler must return generic ApiResponse envelope")

# Public query/domain failures must not fall through to generic HTTP 500.
search_invalid = text("backend/services/be-search-api/src/main/java/com/dnnthanh/marketplace/be/search/api/application/exception/InvalidSearchCriteriaException.java")
require("extends BusinessException" in search_invalid,
        "Search invalid criteria must map through the shared BusinessException contract")
audit_invalid = text("backend/services/be-audit-api/src/main/java/com/dnnthanh/marketplace/be/audit/api/application/exception/InvalidAuditSearchCriteriaException.java")
require("extends BusinessException" in audit_invalid,
        "Audit invalid criteria must map through the shared BusinessException contract")
catalog_domain_handler = text("backend/services/be-catalog-api/src/main/java/com/dnnthanh/marketplace/be/catalog/api/adapter/in/web/CatalogDomainExceptionHandler.java")
require("ProductStateConflictException.class" in catalog_domain_handler and "CatalogErrorCode.PRODUCT_STATE_CONFLICT" in catalog_domain_handler,
        "Catalog domain state conflicts must translate through typed 409 error semantics")
require("InvalidProductException.class" in catalog_domain_handler and "CatalogErrorCode.INVALID_PRODUCT" in catalog_domain_handler,
        "Catalog invalid domain input must translate through typed 400 error semantics")

# MapStruct iterable -> bean bug must be handled explicitly.
mapper = text("backend/services/be-checkout-api/src/main/java/com/dnnthanh/marketplace/be/checkout/api/adapter/out/internal/common/rest/mapper/CheckoutInternalRestMapper.java")
require("default InventoryReleaseRequest toInventoryReleaseRequest" in mapper,
        "Inventory release mapping must be an explicit default method")
require("default PromotionReservationMutationRequest" in mapper,
        "Promotion mutation mapping must be an explicit default method")

# Pageable policy is centralized in the platform starter rather than reimplemented per controller.
pageable_config = text("backend/platform/be-platform-starter/src/main/java/com/dnnthanh/marketplace/be/platform/config/PlatformPageableConfiguration.java")
require("PageableHandlerMethodArgumentResolverCustomizer" in pageable_config,
        "platform starter must centrally configure Pageable argument resolution")
require("setMaxPageSize(100)" in pageable_config,
        "platform starter must enforce a production-safe global max page size")

# Catalog uses Pageable/Page instead of hand-built page/size -> offset/limit transport fields.
catalog_request = text("backend/services/be-catalog-api/src/main/java/com/dnnthanh/marketplace/be/catalog/api/api/request/search/ProductSearchRequest.java")
require("private int page" not in catalog_request and "private int size" not in catalog_request,
        "Catalog ProductSearchRequest must leave pagination to Pageable")
catalog_api = text("backend/services/be-catalog-api/src/main/java/com/dnnthanh/marketplace/be/catalog/api/api/ProductPublicApi.java")
require("Pageable pageable" in catalog_api, "Catalog search API must accept Pageable")
require("ApiResponse" in catalog_api, "Catalog search API must expose generic response envelope")
catalog_repo = text("backend/services/be-catalog-api/src/main/java/com/dnnthanh/marketplace/be/catalog/api/adapter/out/persistence/repository/ProductJpaRepository.java")
require("countQuery" in catalog_repo and "Page<ProductJpaEntity>" in catalog_repo,
        "Catalog native search must provide pageable Page + countQuery")

# Relational list/search APIs use Spring Pageable/Page instead of transport-owned size/LIMIT.
relational_pageable = [
    (
        "Audit",
        "backend/services/be-audit-api/src/main/java/com/dnnthanh/marketplace/be/audit/api/api/request/search/AuditSearchRequest.java",
        "backend/services/be-audit-api/src/main/java/com/dnnthanh/marketplace/be/audit/api/api/AuditApi.java",
        "backend/services/be-audit-api/src/main/java/com/dnnthanh/marketplace/be/audit/api/adapter/out/persistence/repository/AuditSearchJpaRepository.java",
    ),
    (
        "Inventory",
        "backend/services/be-inventory-api/src/main/java/com/dnnthanh/marketplace/be/inventory/api/api/request/search/InventoryBalanceSearchRequest.java",
        "backend/services/be-inventory-api/src/main/java/com/dnnthanh/marketplace/be/inventory/api/api/InventoryPrivateApi.java",
        "backend/services/be-inventory-api/src/main/java/com/dnnthanh/marketplace/be/inventory/api/adapter/out/persistence/repository/InventoryQueryJpaRepository.java",
    ),
    (
        "Payment",
        "backend/services/be-payment-api/src/main/java/com/dnnthanh/marketplace/be/payment/api/api/request/search/PaymentSearchRequest.java",
        "backend/services/be-payment-api/src/main/java/com/dnnthanh/marketplace/be/payment/api/api/PaymentPrivateApi.java",
        "backend/services/be-payment-api/src/main/java/com/dnnthanh/marketplace/be/payment/api/adapter/out/persistence/repository/PaymentQueryJpaRepository.java",
    ),
    (
        "Review",
        "backend/services/be-review-api/src/main/java/com/dnnthanh/marketplace/be/review/api/api/request/search/ReviewSearchRequest.java",
        "backend/services/be-review-api/src/main/java/com/dnnthanh/marketplace/be/review/api/api/ReviewApi.java",
        "backend/services/be-review-api/src/main/java/com/dnnthanh/marketplace/be/review/api/adapter/out/persistence/repository/ProductReviewJpaRepository.java",
    ),
    (
        "Settlement",
        "backend/services/be-settlement-api/src/main/java/com/dnnthanh/marketplace/be/settlement/api/api/request/search/SettlementSearchRequest.java",
        "backend/services/be-settlement-api/src/main/java/com/dnnthanh/marketplace/be/settlement/api/api/SettlementApi.java",
        "backend/services/be-settlement-api/src/main/java/com/dnnthanh/marketplace/be/settlement/api/adapter/out/persistence/repository/SettlementJpaRepository.java",
    ),
]
for name, request_path, api_path, repository_path in relational_pageable:
    request_body = text(request_path)
    api_body = text(api_path)
    repository_body = text(repository_path)
    require("private int size" not in request_body, f"{name} search request must leave size to Pageable")
    require("Pageable pageable" in api_body, f"{name} search API must accept Pageable")
    require("Page<" in repository_body and "countQuery" in repository_body,
            f"{name} native search repository must return Page with countQuery")
    require("criteria.size" not in repository_body,
            f"{name} native search must not bind transport-owned size")

# OpenSearch keeps cursor/search_after, but pagination details move to generic metadata.
search_api = text("backend/services/be-search-api/src/main/java/com/dnnthanh/marketplace/be/search/api/api/SearchApi.java")
require("ApiResponse<SearchResponse>" in search_api, "Search API must return generic ApiResponse")
search_controller = text("backend/services/be-search-api/src/main/java/com/dnnthanh/marketplace/be/search/api/adapter/in/web/SearchController.java")
require("CursorMetadata" in search_controller, "Search controller must expose cursor metadata")
search_response = text("backend/services/be-search-api/src/main/java/com/dnnthanh/marketplace/be/search/api/api/response/SearchResponse.java")
require("nextCursor" not in search_response and "long total" not in search_response,
        "Search pagination fields belong in metadata, not data payload")

# Secrets are mandatory when a service declares secret keys; services without secrets do not
# reference a placeholder Secret at all.
for secret_env in ROOT.glob("ci-cdconfigs/be-*/env/secrets.env.example"):
    service_dir = secret_env.parents[1]
    deployment = service_dir / "kubernetes/base/deployment.yaml"
    deployment_body = deployment.read_text(encoding="utf-8")
    declared = [
        line for line in secret_env.read_text(encoding="utf-8").splitlines()
        if line.strip() and not line.lstrip().startswith("#")
    ]
    if declared:
        require("secretRef:" in deployment_body,
                f"service declaring secrets must reference a Kubernetes Secret: {service_dir.name}")
        require("optional: true" not in deployment_body,
                f"production Secret must not be optional: {service_dir.name}")
    else:
        require("secretRef:" not in deployment_body,
                f"service with no secret keys must not reference an empty Secret: {service_dir.name}")

# Reusable Kubernetes bases must not embed environment-specific runtime values.
require((ROOT / "ci-cdconfigs/apply-service-config.sh").exists(),
        "deployment-time ConfigMap materialization helper missing")
for kustomization in ROOT.glob("ci-cdconfigs/be-*/kubernetes/base/kustomization.yaml"):
    require("configmap.yaml" not in kustomization.read_text(encoding="utf-8"),
            f"reusable base still owns configmap.yaml: {kustomization.relative_to(ROOT)}")
for configmap in ROOT.glob("ci-cdconfigs/be-*/kubernetes/base/configmap.yaml"):
    body = configmap.read_text(encoding="utf-8")
    if re.search(r"jdbc:(postgresql|mysql|oracle|sqlserver):", body, re.I):
        failures.append(f"embedded DB URL in reusable base: {configmap.relative_to(ROOT)}")
    if re.search(r"https?://(?:keycloak|kafka|postgres|mysql|oracle|sqlserver|redis|minio|lgtm|be-[a-z0-9-]+)(?::|/|$)", body, re.I):
        failures.append(f"embedded internal endpoint in reusable base: {configmap.relative_to(ROOT)}")
    if re.search(r"^\s*DB_USERNAME:\s*(?!REPLACE_|\$\{|INJECT_)[^\s]+", body, re.M):
        failures.append(f"embedded DB username in reusable base: {configmap.relative_to(ROOT)}")

if failures:
    print("platform api hardening verification: FAIL")
    for failure in failures:
        print(f" - {failure}")
    sys.exit(1)

print("platform api hardening verification: PASS")
