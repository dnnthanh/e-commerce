#!/usr/bin/env python3
"""Static verification for Feature Spec 013 when the full Java toolchain is unavailable."""
from __future__ import annotations

from pathlib import Path
import re
import sys

ROOT = Path(__file__).resolve().parents[1]
failures: list[str] = []
passes: list[str] = []


def check(condition: bool, message: str) -> None:
    (passes if condition else failures).append(message)


def read(relative: str) -> str:
    return (ROOT / relative).read_text(encoding="utf-8")


parent_pom = read("backend/pom.xml")
agents = read("AGENTS.MD")
conventions = read(".agent/CONVENTIONS.MD")
dockerfile = read("Dockerfile.backend")
use_case = read(
    "backend/platform/be-platform-starter/src/main/java/"
    "com/dnnthanh/marketplace/be/platform/stereotype/UseCase.java"
)
persistence = read(
    "backend/platform/be-platform-starter/src/main/java/"
    "com/dnnthanh/marketplace/be/platform/stereotype/Persistence.java"
)

check("<java.version>25</java.version>" in parent_pom, "parent POM targets Java 25")
check("Target Java 25 LTS" in agents, "AGENTS.md targets Java 25")
check("Java 25" in conventions, "CONVENTIONS.md targets Java 25")
check("eclipse-temurin-25" in dockerfile and "temurin:25-jre" in dockerfile, "backend Dockerfile uses Java 25")
check("<lombok.version>1.18.46</lombok.version>" in parent_pom, "Lombok version supports JDK 25")
check("spotless-maven-plugin" in parent_pom, "Spotless is wired into the parent quality gate")
check("jacoco-maven-plugin" in parent_pom, "JaCoCo is wired into the parent quality gate")
check("@Component" in use_case and "@interface UseCase" in use_case, "@UseCase is a @Component stereotype")
check("@Component" in persistence and "@interface Persistence" in persistence, "@Persistence is a @Component stereotype")

java_roots = [ROOT / "backend/services", ROOT / "backend/workers", ROOT / "backend/outbox"]
java_files = [path for root in java_roots if root.exists() for path in root.rglob("*.java")]
service_imports = [path for path in java_files if "org.springframework.stereotype.Service" in path.read_text(encoding="utf-8")]
check(not service_imports, "generic @Service is not used in bounded-context implementation classes")

persistence_java = [
    path
    for path in java_files
    if "/adapter/out/persistence/" in path.as_posix()
]
repository_imports = [
    path
    for path in persistence_java
    if "org.springframework.stereotype.Repository" in path.read_text(encoding="utf-8")
]
check(not repository_imports, "persistence adapters do not use generic @Repository stereotype")

missing_type = []
type_pattern = re.compile(r"\b(class|interface|record|enum|@interface)\s+[A-Za-z_$][A-Za-z0-9_$]*")
for path in java_files:
    text = path.read_text(encoding="utf-8")
    if not type_pattern.search(text):
        missing_type.append(path)
check(not missing_type, "all backend Java sources still contain a declared type")

order_root = ROOT / "backend/services/be-order-api"
comment_root = ROOT / "backend/services/be-comment-api"
order_pom = (order_root / "pom.xml").read_text(encoding="utf-8")
check("spring-boot-starter-data-jpa" in order_pom, "Order basic persistence uses Spring Data JPA")
check("spring-boot-starter-jdbc" not in order_pom, "Order no longer depends on JDBC starter for basic persistence")

order_main = list((order_root / "src/main/java").rglob("*.java"))
comment_main = list((comment_root / "src/main/java").rglob("*.java"))
for label, files in (("Order", order_main), ("Comment", comment_main)):
    business_files = [p for p in files if "/domain/" in p.as_posix() or "/application/" in p.as_posix()]
    trim_hits = [p for p in business_files if ".trim()" in p.read_text(encoding="utf-8")]
    generic_hits = [p for p in business_files if "IllegalArgumentException" in p.read_text(encoding="utf-8")]
    check(not trim_hits, f"{label} domain/application does not manually trim String input")
    check(not generic_hits, f"{label} domain/application does not use IllegalArgumentException for business errors")

required_order = [
    "src/main/java/com/dnnthanh/marketplace/be/order/api/application/port/in/OrderCommandUseCase.java",
    "src/main/java/com/dnnthanh/marketplace/be/order/api/application/port/in/OrderQueryUseCase.java",
    "src/main/java/com/dnnthanh/marketplace/be/order/api/domain/model/MarketplaceOrder.java",
    "src/main/java/com/dnnthanh/marketplace/be/order/api/adapter/out/persistence/OrderPersistenceAdapter.java",
    "src/main/java/com/dnnthanh/marketplace/be/order/api/api/request/search/OrderSearchRequest.java",
]
check(all((order_root / p).exists() for p in required_order), "Order contains command/query/domain/JPA/search boundaries")

required_comment = [
    "src/main/java/com/dnnthanh/marketplace/be/comment/api/application/port/in/CommentCommandUseCase.java",
    "src/main/java/com/dnnthanh/marketplace/be/comment/api/application/port/in/CommentQueryUseCase.java",
    "src/main/java/com/dnnthanh/marketplace/be/comment/api/domain/model/CommentThread.java",
    "src/main/java/com/dnnthanh/marketplace/be/comment/api/domain/model/CommentReaction.java",
    "src/main/java/com/dnnthanh/marketplace/be/comment/api/domain/model/CommentReport.java",
    "src/main/java/com/dnnthanh/marketplace/be/comment/api/adapter/out/ratelimit/CaffeineCommentRateLimitAdapter.java",
]
check(all((comment_root / p).exists() for p in required_comment), "Comment contains thread/reply/reaction/report/rate-limit boundaries")

# Ensure relational JPA naming and Mongo naming conventions are represented.
check(any(p.name.endswith("JpaEntity.java") for p in order_main), "Order persistence entities use *JpaEntity naming")
check(any(p.name.endswith("Document.java") for p in comment_main), "Comment Mongo persistence uses *Document naming")
check(any(p.name.endswith("Request.java") for p in order_main + comment_main), "HTTP input models use *Request naming")
check(any(p.name.endswith("Response.java") for p in order_main + comment_main), "HTTP output models use *Response naming")

print("Feature 013 static verification")
for message in passes:
    print(f"PASS: {message}")
for message in failures:
    print(f"FAIL: {message}")
print(f"Summary: {len(passes)} passed, {len(failures)} failed")
sys.exit(1 if failures else 0)
