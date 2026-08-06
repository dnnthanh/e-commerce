#!/usr/bin/env python3
from pathlib import Path
import re
import sys

ROOT = Path(__file__).resolve().parents[1]
BACKEND = ROOT / "backend"
checks: list[tuple[str, bool]] = []

def text(rel: str) -> str:
    return (ROOT / rel).read_text()

def check(name: str, ok: bool) -> None:
    checks.append((name, ok))
    print(("PASS" if ok else "FAIL") + ": " + name)

checkout_test = text("backend/services/be-checkout-api/src/test/java/com/dnnthanh/marketplace/be/checkout/api/application/CheckoutOrchestratorTest.java")
check("checkout orchestrator imports service implementation", "import com.dnnthanh.marketplace.be.checkout.api.application.service.CheckoutServiceImplement;" in checkout_test)
check("checkout orchestrator imports Mockito doThrow", "import static org.mockito.Mockito.doThrow;" in checkout_test)

saga_test = text("backend/services/be-checkout-api/src/test/java/com/dnnthanh/marketplace/be/checkout/api/domain/model/CheckoutSagaTest.java")
check("unknown payment uses typed CheckoutPaymentStatus", "saga.payment(\"P1\", CheckoutPaymentStatus.UNKNOWN)" in saga_test and 'saga.payment("P1", "UNKNOWN")' not in saga_test)

business_exception = text("backend/platform/be-platform-starter/src/main/java/com/dnnthanh/marketplace/be/platform/exception/BusinessException.java")
check("business exception serialization metadata is transient", all(token in business_exception for token in ["transient ErrorCode errorCode", "transient Object[] messageArguments", "transient Map<String, Object> details", "serialVersionUID"]))

controllers_with_context = []
for path in (BACKEND / "services").rglob("*Controller.java"):
    if "UserContext" in path.read_text():
        controllers_with_context.append(path)
check("service controllers do not depend on UserContext", not controllers_with_context)

comment_thread = text("backend/services/be-comment-api/src/main/java/com/dnnthanh/marketplace/be/comment/api/domain/model/CommentThread.java")
check("comment create uses draft instead of long parameter list", "create(CommentThreadDraft draft, LocalDateTime now)" in comment_thread)
comment_service = text("backend/services/be-comment-api/src/main/java/com/dnnthanh/marketplace/be/comment/api/application/service/CommentCommandServiceImplement.java")
check("comment service resolves authenticated user", "private final UserContext userContext;" in comment_service and "userContext.userId()" in comment_service)
comment_controller = text("backend/services/be-comment-api/src/main/java/com/dnnthanh/marketplace/be/comment/api/adapter/in/web/CommentController.java")
check("comment controller delegates identity to service", "UserContext" not in comment_controller and "userContext." not in comment_controller)
comment_test = text("backend/services/be-comment-api/src/test/java/com/dnnthanh/marketplace/be/comment/api/domain/model/CommentThreadTest.java")
check("comment tests use encapsulated getters", not re.search(r"\bthread\.(?:content|status|replyCount)\(\)", comment_test))

outbox = text("backend/platform/be-platform-starter/src/main/java/com/dnnthanh/marketplace/be/platform/event/OutboxEventFactory.java")
check("outbox event factory uses Lombok constructor", "@RequiredArgsConstructor" in outbox and not re.search(r"public\s+OutboxEventFactory\s*\(", outbox))

starter_pom = text("backend/platform/be-platform-starter/pom.xml")
check("platform uses Boot OpenTelemetry starter", "spring-boot-starter-opentelemetry" in starter_pom and "micrometer-tracing-bridge-otel" not in starter_pom)
tracer_usage = []
for path in BACKEND.rglob("*.java"):
    content = path.read_text()
    if re.search(r"private\s+final\s+Tracer\s+", content):
        tracer_usage.append(path)
trace_accessor = text("backend/platform/be-platform-starter/src/main/java/com/dnnthanh/marketplace/be/platform/trace/TraceContextAccessor.java")
check("Tracer is optional behind TraceContextAccessor", "ObjectProvider<Tracer>" in trace_accessor and not tracer_usage)

failed = [name for name, ok in checks if not ok]
print(f"REQUESTED_HARDENING_V6: {len(checks)-len(failed)} passed, {len(failed)} failed")
if failed:
    for name in failed:
        print(" - " + name)
    sys.exit(1)
