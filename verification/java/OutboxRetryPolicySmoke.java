import com.dnnthanh.marketplace.be.platform.outbox.OutboxRetryPolicy;
import java.time.Duration;

public final class OutboxRetryPolicySmoke {
  public static void main(String[] args) {
    var policy = new OutboxRetryPolicy(5, Duration.ofSeconds(2), Duration.ofSeconds(30));
    require(policy.nextDelay(1).equals(Duration.ofSeconds(2)), "attempt 1");
    require(policy.nextDelay(2).equals(Duration.ofSeconds(4)), "attempt 2");
    require(policy.nextDelay(5).equals(Duration.ofSeconds(30)), "delay capped");
    require(!policy.terminal(4), "not terminal before max");
    require(policy.terminal(5), "terminal at max");
    System.out.println("OUTBOX_RETRY_POLICY_SMOKE=PASS");
  }
  private static void require(boolean condition, String message) {
    if (!condition) throw new AssertionError(message);
  }
}
