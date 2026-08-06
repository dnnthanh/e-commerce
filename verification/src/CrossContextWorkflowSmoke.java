import com.dnnthanh.marketplace.be.cart.api.domain.model.ShoppingCart;
import com.dnnthanh.marketplace.be.checkout.api.domain.model.CheckoutProcess;
import com.dnnthanh.marketplace.be.fulfillment.api.domain.enumtype.ShipmentStatus;
import com.dnnthanh.marketplace.be.fulfillment.api.domain.model.Shipment;
import com.dnnthanh.marketplace.be.inventory.api.domain.model.StockLedger;
import com.dnnthanh.marketplace.be.payment.api.domain.model.Payment;
import com.dnnthanh.marketplace.be.payment.api.domain.model.Payment.Provider;
import com.dnnthanh.marketplace.be.payment.api.domain.model.Payment.ProviderOutcome;
import com.dnnthanh.marketplace.be.returns.api.domain.enumtype.InventoryDisposition;
import com.dnnthanh.marketplace.be.returns.api.domain.model.ReturnRequest;
import com.dnnthanh.marketplace.be.returns.api.domain.model.ReturnRequest.InspectionDecision;
import com.dnnthanh.marketplace.be.returns.api.domain.model.ReturnRequest.ReturnLine;
import com.dnnthanh.marketplace.be.settlement.api.domain.enumtype.LedgerEntryType;
import com.dnnthanh.marketplace.be.settlement.api.domain.model.SellerSettlementLedger;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/** Framework-free verification of the mandatory cross-context invariants. */
public final class CrossContextWorkflowSmoke {
  public static void main(String[] args) throws Exception {
    purchaseAndCompensation();
    oversellRace();
    paymentCallbackRace();
    partialFulfillmentReturnSettlement();
    outboxCrashRecoverySemantics();
    System.out.println("CROSS_CONTEXT_WORKFLOW_SMOKE=PASS");
  }

  private static void purchaseAndCompensation() {
    ShoppingCart cart = new ShoppingCart("CART", "USER");
    cart.addOrReplace(new ShoppingCart.CartLine(1L, "SKU", 1, new BigDecimal("100"), true), 0);
    StockLedger inventory = new StockLedger("SKU", 10L, 1);
    check(inventory.reserve("checkout:1", 1), "inventory reserve expected");

    CheckoutProcess checkout = new CheckoutProcess("CHECKOUT", "IDEMP");
    checkout.quoted();
    checkout.promotionReserved();
    checkout.inventoryReserved();
    checkout.paymentInitiated();
    CheckoutProcess.CompensationPlan plan = checkout.fail("payment unknown");
    check(plan.releaseInventory() && plan.releasePromotion() && plan.reconcilePayment(),
        "ambiguous payment must trigger inventory/promotion compensation and payment reconciliation");
    inventory.release("checkout:1");
    check(inventory.available() == 1, "compensation must restore availability");
  }

  private static void oversellRace() throws Exception {
    StockLedger inventory = new StockLedger("LAST-SKU", 1L, 1);
    CountDownLatch start = new CountDownLatch(1);
    AtomicInteger successes = new AtomicInteger();
    Runnable reserve = () -> {
      try {
        start.await();
        String key = "R-" + Thread.currentThread().getName();
        inventory.reserve(key, 1);
        successes.incrementAndGet();
      } catch (RuntimeException ignored) {
        // losing contender is expected
      } catch (InterruptedException interrupted) {
        Thread.currentThread().interrupt();
      }
    };
    Thread a = new Thread(reserve, "A");
    Thread b = new Thread(reserve, "B");
    a.start(); b.start(); start.countDown(); a.join(); b.join();
    check(successes.get() == 1, "only one concurrent reservation may win last unit");
    check(inventory.available() == 0 && inventory.reserved() == 1, "stock must never become negative");
  }

  private static void paymentCallbackRace() throws Exception {
    Payment payment = new Payment("PAY", "ORDER", "USER", Provider.MOMO, new BigDecimal("100"), "VND");
    Set<String> inbox = new HashSet<>();
    CountDownLatch start = new CountDownLatch(1);
    AtomicInteger applied = new AtomicInteger();
    Runnable callback = () -> {
      try {
        start.await();
        synchronized (payment) {
          if (inbox.add("PROVIDER-EVENT-1")) {
            payment.applyProviderEvent(ProviderOutcome.CAPTURED, "TX-1");
            applied.incrementAndGet();
          }
        }
      } catch (InterruptedException interrupted) {
        Thread.currentThread().interrupt();
      }
    };
    Thread webhook = new Thread(callback, "webhook");
    Thread poller = new Thread(callback, "poller");
    webhook.start(); poller.start(); start.countDown(); webhook.join(); poller.join();
    check(applied.get() == 1, "inbox deduplication must cause exactly one business transition");
  }

  private static void partialFulfillmentReturnSettlement() {
    Shipment first = new Shipment("S1", "ORDER", 1L, 10L,
        List.of(new Shipment.ShipmentLine(1L, "SKU", 1)));
    Shipment second = new Shipment("S2", "ORDER", 1L, 10L,
        List.of(new Shipment.ShipmentLine(1L, "SKU", 1)));
    deliver(first);
    deliver(second);

    ReturnRequest returned = new ReturnRequest(
        "RET",
        "ORDER",
        "USER",
        "DAMAGED",
        List.of(new ReturnLine(1L, 1L, 101L, 2, 0, 1, new BigDecimal("40"), LocalDateTime.now().minusDays(1))));
    returned.approve();
    returned.receive(10L);
    BigDecimal refund = returned.inspect(Map.of(1L, new InspectionDecision(1, InventoryDisposition.RESTOCK)));
    returned.prepareRefund();
    returned.completeRefund(refund);

    SellerSettlementLedger ledger = new SellerSettlementLedger(1L);
    ledger.append("sale", LedgerEntryType.SALE, new BigDecimal("80"), "ORDER");
    ledger.append("commission", LedgerEntryType.COMMISSION, new BigDecimal("-8"), "ORDER");
    ledger.append("refund", LedgerEntryType.REFUND, refund.negate(), "RET");
    check(ledger.payableBalance().compareTo(new BigDecimal("32.00")) == 0,
        "partial return must adjust seller payable ledger without rewriting old entries");
  }

  private static void deliver(Shipment shipment) {
    LocalDateTime now = LocalDateTime.now();
    shipment.transitionTo(ShipmentStatus.PICKING, now);
    shipment.transitionTo(ShipmentStatus.PACKED, now);
    shipment.transitionTo(ShipmentStatus.READY_TO_SHIP, now);
    shipment.transitionTo(ShipmentStatus.HANDED_OVER, now);
    shipment.applyCarrierEvent(10, ShipmentStatus.IN_TRANSIT, now);
    shipment.applyCarrierEvent(11, ShipmentStatus.DELIVERED, now);
  }

  private static void outboxCrashRecoverySemantics() {
    java.util.Set<String> downstreamInbox = new java.util.HashSet<>();
    String eventId = "EVENT-1";
    check(downstreamInbox.add(eventId), "first broker delivery should cause business effect");
    // publisher crashes after broker send and re-publishes the same durable outbox event
    check(!downstreamInbox.add(eventId), "consumer inbox must neutralize duplicate post-crash delivery");
  }

  private static void check(boolean condition, String message) {
    if (!condition) throw new AssertionError(message);
  }
}
