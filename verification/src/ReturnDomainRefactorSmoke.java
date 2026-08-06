import com.dnnthanh.marketplace.be.returns.api.domain.enumtype.ReturnStatus;
import com.dnnthanh.marketplace.be.returns.api.domain.exception.ReturnStateConflictException;
import com.dnnthanh.marketplace.be.returns.api.domain.model.ReturnRequest;
import java.math.BigDecimal;
import java.util.List;

/** Framework-free RED/GREEN smoke for typed return lifecycle. */
public final class ReturnDomainRefactorSmoke {
  private ReturnDomainRefactorSmoke() {}

  public static void main(String[] args) {
    ReturnRequest request = new ReturnRequest(
        "RET-1",
        "ORD-1",
        "USR-1",
        "damaged",
        List.of(new ReturnRequest.ReturnLine(10L, 20L, 30L, 1, new BigDecimal("125000.00"))));

    require(request.status() == ReturnStatus.REQUESTED, "new return must be REQUESTED");
    request.approve();
    require(request.status() == ReturnStatus.APPROVED, "approved return must be APPROVED");
    request.receive();
    require(request.status() == ReturnStatus.RECEIVED, "received return must be RECEIVED");
    request.prepareRefund();
    require(request.status() == ReturnStatus.REFUND_PENDING, "refund must be pending before remote call");
    request.markRefundUnknown();
    require(request.status() == ReturnStatus.REFUND_UNKNOWN, "unknown result must remain recoverable");
    request.retryRefund();
    require(request.status() == ReturnStatus.REFUND_PENDING, "unknown result must be retryable");
    request.completeRefund();
    require(request.status() == ReturnStatus.COMPLETED, "durable refund event completes return");

    try {
      request.approve();
      throw new AssertionError("completed return must reject approval");
    } catch (ReturnStateConflictException expected) {
      // expected
    }

    System.out.println("RETURN_DOMAIN_REFACTOR_SMOKE=PASS");
  }

  private static void require(boolean condition, String message) {
    if (!condition) {
      throw new AssertionError(message);
    }
  }
}
