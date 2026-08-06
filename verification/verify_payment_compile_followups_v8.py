from pathlib import Path
import sys

root = Path(__file__).resolve().parents[1] / "backend"
checks = []

def check(name, ok):
    checks.append((name, bool(ok)))

adapter = (root / "services/be-payment-api/src/main/java/com/dnnthanh/marketplace/be/payment/api/adapter/out/persistence/JdbcPaymentPersistenceAdapter.java").read_text()
refund_port = (root / "services/be-payment-api/src/main/java/com/dnnthanh/marketplace/be/payment/api/application/port/out/RefundPersistencePort.java").read_text()
refund_service = (root / "services/be-payment-api/src/main/java/com/dnnthanh/marketplace/be/payment/api/application/service/RefundServiceImplement.java").read_text()
private_api = (root / "services/be-payment-api/src/main/java/com/dnnthanh/marketplace/be/payment/api/api/PaymentPrivateApi.java").read_text()
internal_api = (root / "services/be-payment-api/src/main/java/com/dnnthanh/marketplace/be/payment/api/api/PaymentInternalApi.java").read_text()
pom = (root / "pom.xml").read_text()
provider_props = (root / "services/be-payment-api/src/main/java/com/dnnthanh/marketplace/be/payment/api/config/PaymentProviderProperties.java").read_text()
inv_exc = (root / "services/be-inventory-api/src/main/java/com/dnnthanh/marketplace/be/inventory/api/domain/exception/InsufficientStockException.java").read_text()
inv_code = root / "services/be-inventory-api/src/main/java/com/dnnthanh/marketplace/be/inventory/api/domain/exception/InventoryErrorCode.java"

check("payment adapter has one payment findByKey", adapter.count("Optional<Payment> findByKey(String key)") == 1)
check("refund persistence uses distinct findRefundByKey", "findRefundByKey(String refundKey)" in refund_port)
check("payment adapter implements distinct refund lookup", "Optional<RefundSnapshot> findRefundByKey(String refundKey)" in adapter)
check("refund service calls distinct refund lookup", "refunds.findRefundByKey(refundKey)" in refund_service)
check("payment private api imports CreatePaymentRequest", "import com.dnnthanh.marketplace.be.payment.api.api.request.CreatePaymentRequest;" in private_api)
check("payment private api imports PaymentResponse", "import com.dnnthanh.marketplace.be.payment.api.api.response.PaymentResponse;" in private_api)
check("payment internal api imports PaymentResponse", "import com.dnnthanh.marketplace.be.payment.api.api.response.PaymentResponse;" in internal_api)
check("jdk25 annotation processing explicitly enabled", "<proc>full</proc>" in pom)
check("payment provider properties uses immutable record binding", "record PaymentProviderProperties" in provider_props and "String simulator" in provider_props)
app = (root / "services/be-payment-api/src/main/java/com/dnnthanh/marketplace/be/payment/api/PaymentApiApplication.java").read_text()
adapter_sim = (root / "services/be-payment-api/src/main/java/com/dnnthanh/marketplace/be/payment/api/adapter/out/internal/paymentsimulator/rest/AbstractPaymentSimulatorRestAdapter.java").read_text()
check("payment provider properties explicitly enabled", "@EnableConfigurationProperties(PaymentProviderProperties.class)" in app)
check("payment simulator uses record accessor", "properties.simulator()" in adapter_sim)
check("insufficient stock is a BusinessException", "extends BusinessException" in inv_exc)
check("inventory typed error code exists", inv_code.exists() and "implements ErrorCode" in inv_code.read_text())

failed = [name for name, ok in checks if not ok]
for name, ok in checks:
    print(("PASS" if ok else "FAIL") + ": " + name)
print(f"PAYMENT_COMPILE_FOLLOWUPS_V8: {len(checks)-len(failed)} passed, {len(failed)} failed")
sys.exit(1 if failed else 0)
