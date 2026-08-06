from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]
failures = []
passes = []

def check(name: str, condition: bool, detail: str = "") -> None:
    (passes if condition else failures).append((name, detail))

parent = (ROOT / "backend/pom.xml").read_text(encoding="utf-8")
agents = (ROOT / "AGENTS.MD").read_text(encoding="utf-8")
conventions = (ROOT / ".agent/CONVENTIONS.MD").read_text(encoding="utf-8")
backend_docker = (ROOT / "Dockerfile.backend").read_text(encoding="utf-8")
jmx_docker = (ROOT / "infrastructure/kafka/jmx-exporter/Dockerfile").read_text(encoding="utf-8")
spec = (ROOT / ".agent/specs/013-codebase-deep-refactor.md").read_text(encoding="utf-8")

check("parent Java 25", "<java.version>25</java.version>" in parent)
check("compiler release follows Java 25", "<release>${java.version}</release>" in parent)
check("AGENTS Java 25", "Target Java 25 LTS" in agents)
check("CONVENTIONS Java 25", "Java 25 LTS" in conventions)
check("backend build/runtime images Java 25", "eclipse-temurin-25" in backend_docker and "temurin:25-jre" in backend_docker)
check("Kafka JMX exporter runtime Java 25", "temurin:25-jre" in jmx_docker)
check("spec no stale Java-21 default", "Java 21 is the default" not in spec)
check("spec knows Maven Wrapper exists", "no Maven Wrapper is present" not in spec)

required_docs = {
    "catalog-product-publish-and-variant",
    "cart-authoritative-checkout-validation",
    "checkout-saga-compensation",
    "promotion-targeting-and-reservation",
    "inventory-oversell-and-reconciliation",
    "order-lifecycle-and-operations-override",
    "payment-webhook-idempotency-and-refund",
    "fulfillment-multi-package-and-reconciliation",
    "return-partial-refund-disposition-and-dispute",
    "review-verified-purchase-and-moderation",
    "comment-thread-moderation-and-reaction",
    "notification-delivery-retry-dlq-replay",
    "authorization-durable-provider-mutation-recovery",
    "settlement-ledger-and-reconciliation",
    "search-stale-event-and-reindex",
    "media-processing-idempotency-and-safe-delete",
}
for folder in sorted(required_docs):
    check(f"usecase documented: {folder}", (ROOT / "usecase" / folder / "README.md").is_file())

api_modules = [
    "be-audit-api", "be-authorization-api", "be-cart-api", "be-catalog-api", "be-checkout-api",
    "be-comment-api", "be-fulfillment-api", "be-inventory-api", "be-media-api", "be-notification-api",
    "be-operations-api", "be-order-api", "be-payment-api", "be-pricing-api", "be-promotion-api",
    "be-return-api", "be-review-api", "be-search-api", "be-seller-api", "be-settlement-api",
]
for module in api_modules:
    module_root = ROOT / "backend/services" / module
    java_sources = list((module_root / "src/main/java").rglob("*.java"))
    usecase_sources = [p for p in java_sources if "@UseCase" in p.read_text(encoding="utf-8", errors="ignore")]
    tests = list((module_root / "src/test").rglob("*.java")) if (module_root / "src/test").exists() else []
    check(f"{module} has application use case", bool(usecase_sources), f"found={len(usecase_sources)}")
    check(f"{module} has backend test source", bool(tests), f"found={len(tests)}")

# Retired dual-path aggregate names must not remain in tests after consolidation.
retired_test_patterns = {
    "Cart": "backend/services/be-cart-api/src/test",
    "Review": "backend/services/be-review-api/src/test",
    "PaymentAggregate": "backend/services/be-payment-api/src/test",
    "ReturnCase": "backend/services/be-return-api/src/test",
}
for retired, relative in retired_test_patterns.items():
    base = ROOT / relative
    text = "\n".join(
        file.read_text(encoding="utf-8", errors="ignore")
        for file in base.rglob("*.java")
    ) if base.exists() else ""
    check(f"retired test model absent: {retired}", not re.search(rf"\b{retired}\b", text))

# The final evidence directory is a stable index, separate from historical checkpoint logs.
evidence_dir = ROOT / ".agent/reports/013-codebase-deep-refactor/final-audit"
for filename in ["README.md", "usecase-coverage.md", "test-evidence-index.md"]:
    check(f"final evidence file: {filename}", (evidence_dir / filename).is_file())

# Prevent re-introducing compressed source that cannot be reviewed sanely.
compressed = []
for p in (ROOT / "backend").rglob("*.java"):
    lines = p.read_text(encoding="utf-8", errors="ignore").splitlines()
    if lines and ((len(lines) <= 3 and sum(map(len, lines)) > 500) or max(map(len, lines)) > 500):
        compressed.append(p.relative_to(ROOT).as_posix())
check("no compressed Java files", not compressed, "\n".join(compressed))

for name, detail in passes:
    print(f"PASS: {name}" + (f" ({detail})" if detail else ""))
for name, detail in failures:
    print(f"FAIL: {name}" + (f" ({detail})" if detail else ""))
print(f"SUMMARY: {len(passes)} passed, {len(failures)} failed")
raise SystemExit(1 if failures else 0)
