import com.dnnthanh.marketplace.be.fulfillment.api.domain.enumtype.ShipmentStatus;
import com.dnnthanh.marketplace.be.fulfillment.api.domain.model.Shipment;
import com.dnnthanh.marketplace.be.pricing.api.domain.enumtype.PriceSource;
import com.dnnthanh.marketplace.be.pricing.api.domain.model.PriceRule;
import com.dnnthanh.marketplace.be.pricing.api.domain.service.EffectivePriceResolver;
import com.dnnthanh.marketplace.be.promotion.api.domain.enumtype.PromotionBenefitType;
import com.dnnthanh.marketplace.be.promotion.api.domain.model.PromotionCandidate;
import com.dnnthanh.marketplace.be.promotion.api.domain.service.PromotionEngine;
import com.dnnthanh.marketplace.be.inventory.api.domain.model.StockLedger;
import com.dnnthanh.marketplace.be.cart.api.domain.model.ShoppingCart;
import com.dnnthanh.marketplace.be.checkout.api.domain.enumtype.CheckoutStep;
import com.dnnthanh.marketplace.be.checkout.api.domain.model.CheckoutProcess;
import com.dnnthanh.marketplace.be.payment.api.domain.model.Payment;
import com.dnnthanh.marketplace.be.payment.api.domain.model.Payment.Provider;
import com.dnnthanh.marketplace.be.payment.api.domain.model.Payment.ProviderOutcome;
import com.dnnthanh.marketplace.be.payment.api.domain.model.Payment.Status;
import com.dnnthanh.marketplace.be.returns.api.domain.enumtype.InventoryDisposition;
import com.dnnthanh.marketplace.be.returns.api.domain.model.ReturnRequest;
import com.dnnthanh.marketplace.be.returns.api.domain.model.ReturnRequest.InspectionDecision;
import com.dnnthanh.marketplace.be.returns.api.domain.model.ReturnRequest.ReturnLine;
import com.dnnthanh.marketplace.be.review.api.domain.model.ProductReview;
import com.dnnthanh.marketplace.be.review.api.domain.enumtype.ReviewStatus;
import com.dnnthanh.marketplace.be.seller.api.domain.model.SellerAccount;
import com.dnnthanh.marketplace.be.search.api.domain.model.SearchIndexVersion;
import com.dnnthanh.marketplace.be.notification.api.domain.enumtype.NotificationChannel;
import com.dnnthanh.marketplace.be.notification.api.domain.model.NotificationPreference;
import com.dnnthanh.marketplace.be.notification.api.domain.model.NotificationDelivery;
import com.dnnthanh.marketplace.be.settlement.api.domain.enumtype.LedgerEntryType;
import com.dnnthanh.marketplace.be.settlement.api.domain.model.SellerSettlementLedger;
import com.dnnthanh.marketplace.be.catalog.api.domain.enumtype.AttributeType;
import com.dnnthanh.marketplace.be.catalog.api.domain.model.AttributeDefinition;
import com.dnnthanh.marketplace.be.catalog.api.domain.service.SkuCombinationGenerator;
import com.dnnthanh.marketplace.be.media.api.domain.model.MediaAsset;
import com.dnnthanh.marketplace.be.media.api.domain.enumtype.MediaStatus;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

public final class ProductionDepthDomainSmoke {
  public static void main(String[] args) {
    fulfillment();
    pricing();
    promotion();
    inventory();
    cart();
    checkout();
    payment();
    returnsFlow();
    review();
    seller();
    search();
    notification();
    settlement();
    catalog();
    media();
    System.out.println("PRODUCTION_DEPTH_DOMAIN_SMOKE=PASS");
  }

  private static void fulfillment() {
    Shipment shipment = new Shipment("S1", "O1", 10L, 20L,
        List.of(new Shipment.ShipmentLine(1L, "SKU-1", 2)));
    shipment.transitionTo(ShipmentStatus.PICKING, LocalDateTime.now());
    shipment.transitionTo(ShipmentStatus.PACKED, LocalDateTime.now());
    shipment.transitionTo(ShipmentStatus.READY_TO_SHIP, LocalDateTime.now());
    shipment.assignCarrier("GHN", "TRK1");
    shipment.transitionTo(ShipmentStatus.HANDED_OVER, LocalDateTime.now());
    boolean applied = shipment.applyCarrierEvent(10, ShipmentStatus.IN_TRANSIT, LocalDateTime.now());
    check(applied && !shipment.applyCarrierEvent(9, ShipmentStatus.DELIVERED, LocalDateTime.now()),
        "carrier callbacks must reject stale sequence");
  }

  private static void pricing() {
    LocalDateTime now = LocalDateTime.now();
    List<PriceRule> rules = List.of(
        new PriceRule("base", "SKU", null, null, "VND", new BigDecimal("100000"), PriceSource.BASE, 1, null, null),
        new PriceRule("seller", "SKU", 1L, null, "VND", new BigDecimal("95000"), PriceSource.SELLER, 10, now.minusHours(1), now.plusHours(1)));
    PriceRule selected = new EffectivePriceResolver().resolve(rules, new PriceRule.PriceContext("SKU", 1L, "WEB", now));
    check("seller".equals(selected.ruleId()), "higher priority seller price must win");
  }

  private static void promotion() {
    PromotionCandidate high = new PromotionCandidate("P10", PromotionBenefitType.PERCENTAGE,
        new BigDecimal("10"), new BigDecimal("100"), Set.of(1L), Set.of("SKU"), "G1", 10,
        null, null, 1000, 2);
    PromotionCandidate low = new PromotionCandidate("P20", PromotionBenefitType.FIXED_AMOUNT,
        new BigDecimal("20"), BigDecimal.ZERO, Set.of(1L), Set.of("SKU"), "G1", 5,
        null, null, 1000, 2);
    PromotionEngine.PromotionContext ctx = new PromotionEngine.PromotionContext() {
      public BigDecimal subtotal() { return new BigDecimal("200"); }
      public Long sellerId() { return 1L; }
      public Set<String> skus() { return Set.of("SKU"); }
      public LocalDateTime at() { return LocalDateTime.now(); }
      public long globalUsage(String id) { return 0; }
      public long customerUsage(String id) { return 0; }
    };
    PromotionEngine.Evaluation result = new PromotionEngine().evaluate(List.of(low, high), ctx);
    check(result.totalDiscount().compareTo(new BigDecimal("20.00")) == 0,
        "exclusion group must allow only highest-priority promotion");
    check(result.decisions().stream().filter(PromotionEngine.Decision::applied).count() == 1,
        "one promotion in exclusion group should apply");
  }

  private static void inventory() {
    StockLedger ledger = new StockLedger("SKU", 1L, 5);
    check(ledger.reserve("REQ1", 5), "first reserve must mutate stock");
    check(!ledger.reserve("REQ1", 5), "same idempotency key must be harmless");
    expectFailure(() -> ledger.reserve("REQ2", 1), "oversell must fail");
    ledger.confirm("REQ1");
    check(ledger.onHand() == 0 && ledger.reserved() == 0, "confirm must consume stock");
  }

  private static void cart() {
    ShoppingCart account = new ShoppingCart("A", "U");
    account.addOrReplace(new ShoppingCart.CartLine(1L, "SKU", 1, new BigDecimal("10"), true), 0);
    ShoppingCart guest = new ShoppingCart("G", null);
    guest.addOrReplace(new ShoppingCart.CartLine(1L, "SKU", 3, new BigDecimal("10"), true), 0);
    account.mergeGuest(guest, 1, 5);
    check(account.lines().get(0).quantity() == 3, "guest merge must choose deterministic max quantity");
    expectFailure(() -> account.remove(1L, "SKU", 1), "stale cart version must fail");
  }

  private static void checkout() {
    CheckoutProcess process = new CheckoutProcess("C1", "IDEMP1");
    process.quoted();
    process.promotionReserved();
    process.inventoryReserved();
    process.paymentInitiated();
    CheckoutProcess.CompensationPlan plan = process.fail("provider timeout");
    check(plan.releaseInventory() && plan.releasePromotion() && plan.reconcilePayment(),
        "checkout failure after payment initiation must compensate and reconcile");
    process.compensationCompleted();
    check(process.step() == CheckoutStep.FAILED, "compensation completion must end failed");
  }

  private static void payment() {
    Payment payment = new Payment("P1", "O1", "U1", Provider.MOMO, new BigDecimal("100"), "VND");
    payment.applyProviderEvent(ProviderOutcome.CAPTURED, "TX-1");
    check(payment.status() == Status.PAID, "capture should apply");
    payment.applyProviderEvent(ProviderOutcome.CAPTURED, "TX-1");
    check(payment.status() == Status.PAID, "replayed captured outcome must remain idempotent");
    payment.applySuccessfulRefund(new BigDecimal("40"));
    check(payment.status() == Status.PARTIALLY_REFUNDED, "partial refund state expected");
    expectFailure(() -> payment.applySuccessfulRefund(new BigDecimal("70")), "refund above captured amount must fail");
  }

  private static void returnsFlow() {
    ReturnRequest returned = new ReturnRequest(
        "R1",
        "O1",
        "U1",
        "DAMAGED",
        List.of(new ReturnLine(1L, 1L, 101L, 3, 1, 2, new BigDecimal("25"), LocalDateTime.now().minusDays(1))));
    returned.approve();
    returned.receive(10L);
    BigDecimal amount = returned.inspect(Map.of(1L, new InspectionDecision(1, InventoryDisposition.RESTOCK)));
    check(amount.compareTo(new BigDecimal("25.00")) == 0, "inspection refund amount must use accepted quantity");
    returned.prepareRefund();
    returned.completeRefund(amount);
  }

  private static void review() {
    ProductReview review = new ProductReview(1L, "U1", 10L, 20L, 5, "great", "ok", LocalDateTime.now());
    check(review.markHelpful("U2"), "first helpful reaction should apply");
    check(!review.markHelpful("U2"), "helpful reaction must be idempotent");
    review.edit("U1", 4, "updated", "content", LocalDateTime.now().plusMinutes(5), Duration.ofHours(24));
    review.requireModeration();
    review.hide();
    check(review.status() == ReviewStatus.HIDDEN, "moderation hide expected");
  }

  private static void seller() {
    SellerAccount seller = new SellerAccount(1L);
    seller.submit(); seller.verify(); seller.activate();
    seller.addStaff("U1", Set.of("ORDER_VIEW", "PRODUCT_UPDATE"));
    check(seller.hasPermission("U1", "ORDER_VIEW"), "seller-scoped staff permission expected");
    seller.suspend();
  }

  private static void search() {
    SearchIndexVersion guard = new SearchIndexVersion();
    check(guard.accept("P1", 10), "new index version should apply");
    check(!guard.accept("P1", 9), "stale index event must be rejected");
    check(!guard.accept("P1", 10), "duplicate index event must be rejected");
  }

  private static void notification() {
    NotificationPreference preference = new NotificationPreference("U1",
        EnumSet.of(NotificationChannel.IN_APP, NotificationChannel.EMAIL), LocalTime.of(22, 0), LocalTime.of(7, 0));
    check(preference.mayDeliver(NotificationChannel.IN_APP, LocalTime.MIDNIGHT), "in-app remains durable during quiet hours");
    check(!preference.mayDeliver(NotificationChannel.EMAIL, LocalTime.of(23, 0)), "email must respect quiet hours");
    NotificationDelivery delivery = new NotificationDelivery("N1", "event:U1", "U1", NotificationChannel.EMAIL);
    check(delivery.startAttempt("A1"), "first provider attempt should start");
    check(!delivery.startAttempt("A1"), "provider attempt must be idempotent");
  }

  private static void settlement() {
    SellerSettlementLedger ledger = new SellerSettlementLedger(1L);
    check(ledger.append("E1", LedgerEntryType.SALE, new BigDecimal("100"), "O1"), "sale event should append");
    check(!ledger.append("E1", LedgerEntryType.SALE, new BigDecimal("100"), "O1"), "duplicate settlement event must be ignored");
    ledger.append("E2", LedgerEntryType.COMMISSION, new BigDecimal("-10"), "O1");
    ledger.append("E3", LedgerEntryType.REFUND, new BigDecimal("-20"), "R1");
    check(ledger.payableBalance().compareTo(new BigDecimal("70.00")) == 0, "ledger rebuild must produce payable balance");
  }

  private static void catalog() {
    AttributeDefinition size = new AttributeDefinition(1L, "size", AttributeType.SINGLE_SELECT, true, true,
        List.of("S", "M"), null, null);
    size.validate("M");
    expectFailure(() -> size.validate("XL"), "invalid dynamic attribute option must fail");
    Map<String, List<String>> attrs = new LinkedHashMap<>();
    attrs.put("size", List.of("S", "M"));
    attrs.put("color", List.of("black", "white"));
    check(new SkuCombinationGenerator().generate(attrs).size() == 4, "variant cartesian product expected");
  }

  private static void media() {
    MediaAsset media = new MediaAsset("M1", "S1", "sha256");
    media.uploaded(); media.scanning(); media.scanPassed();
    check(media.variantGenerated("240.webp"), "first generated variant should apply");
    check(!media.variantGenerated("240.webp"), "variant generation must be idempotent");
    media.ready(); media.attach();
    expectFailure(media::requestDelete, "referenced media cannot be deleted");
    media.detach(); media.requestDelete(); media.deleted();
    check(media.status() == MediaStatus.DELETED, "media deletion lifecycle expected");
  }

  private static void check(boolean condition, String message) {
    if (!condition) throw new AssertionError(message);
  }

  private static void expectFailure(Runnable action, String message) {
    try { action.run(); } catch (RuntimeException expected) { return; }
    throw new AssertionError(message);
  }
}
