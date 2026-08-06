package com.dnnthanh.marketplace.be.order.api.domain.model;

import com.dnnthanh.marketplace.be.order.api.domain.enumtype.CancellationReason;
import com.dnnthanh.marketplace.be.order.api.domain.enumtype.OrderStatus;
import com.dnnthanh.marketplace.be.order.api.domain.enumtype.SellerOrderStatus;
import com.dnnthanh.marketplace.be.order.api.domain.exception.InvalidOrderStateException;
import com.dnnthanh.marketplace.be.order.api.domain.exception.InvalidOrderValidationException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.Getter;

/**
 * Multi-seller marketplace order aggregate.
 *
 * <p>The aggregate owns the immutable checkout monetary snapshot and protects parent/child
 * lifecycle transitions. Persistence identifiers may be absent before the first save.
 */
@Getter
public final class MarketplaceOrder {

    private final String orderNo;

    private final String checkoutKey;

    private final String userId;

    private final BigDecimal grossAmount;

    private final BigDecimal discountAmount;

    private final BigDecimal payableAmount;

    private final List<SellerOrder> sellerOrders;

    private OrderStatus status;

    private CancellationReason cancellationReason;

    private final LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private long version;

    private MarketplaceOrder(
            String orderNo,
            String checkoutKey,
            String userId,
            BigDecimal grossAmount,
            BigDecimal discountAmount,
            List<SellerOrder> sellerOrders,
            OrderStatus status,
            CancellationReason cancellationReason,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            long version) {
        this.orderNo = requireText(orderNo, "orderNo");
        this.checkoutKey = requireText(checkoutKey, "checkoutKey");
        this.userId = requireText(userId, "userId");
        this.grossAmount = grossAmount;
        this.discountAmount = discountAmount;
        this.payableAmount = grossAmount.subtract(discountAmount);
        this.sellerOrders = new ArrayList<>(sellerOrders);
        this.status = Objects.requireNonNull(status, "status");
        this.cancellationReason = cancellationReason;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
        this.version = version;
    }

    public static MarketplaceOrder create(
            String orderNo,
            String checkoutKey,
            String userId,
            List<LineCommand> lines,
            BigDecimal grossAmount,
            BigDecimal discountAmount,
            LocalDateTime now) {
        validateMoney(lines, grossAmount, discountAmount);
        List<SellerOrder> sellerOrders =
                allocateSellerOrders(orderNo, lines, grossAmount, discountAmount);
        return new MarketplaceOrder(
                orderNo,
                checkoutKey,
                userId,
                grossAmount,
                discountAmount,
                sellerOrders,
                OrderStatus.CREATED,
                null,
                now,
                now,
                0L);
    }

    /**
     * Rehydrates a persisted aggregate without re-running creation-side validation or allocation.
     */
    public static MarketplaceOrder rehydrate(
            String orderNo,
            String checkoutKey,
            String userId,
            BigDecimal grossAmount,
            BigDecimal discountAmount,
            List<SellerOrder> sellerOrders,
            OrderStatus status,
            CancellationReason cancellationReason,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            long version) {
        return new MarketplaceOrder(
                orderNo,
                checkoutKey,
                userId,
                grossAmount,
                discountAmount,
                sellerOrders,
                status,
                cancellationReason,
                createdAt,
                updatedAt,
                version);
    }

    /** Moves a freshly created order into payment processing. */
    public void markPaymentPending() {
        transition(OrderStatus.CREATED, OrderStatus.PAYMENT_PENDING);
    }

    /** Records successful payment exactly once at the aggregate level. */
    public void markPaid() {
        if (status == OrderStatus.PAID
                || status == OrderStatus.FULFILLING
                || status == OrderStatus.COMPLETED) {
            return;
        }
        transition(OrderStatus.PAYMENT_PENDING, OrderStatus.PAID);
        sellerOrders.replaceAll(SellerOrder::markPaid);
    }

    /** Moves a paid order into fulfillment. */
    public void markFulfilling() {
        if (status == OrderStatus.FULFILLING || status == OrderStatus.COMPLETED) {
            return;
        }
        transition(OrderStatus.PAID, OrderStatus.FULFILLING);
        sellerOrders.replaceAll(SellerOrder::markFulfilling);
    }

    /** Marks fulfillment complete. */
    public void markCompleted() {
        if (status == OrderStatus.COMPLETED) {
            return;
        }
        transition(OrderStatus.FULFILLING, OrderStatus.COMPLETED);
        sellerOrders.replaceAll(SellerOrder::markCompleted);
    }

    /** Cancels an unpaid order and keeps the reason as part of the audit snapshot. */
    public void cancel(CancellationReason reason) {
        Objects.requireNonNull(reason, "reason");
        if (status == OrderStatus.CANCELLED) {
            return;
        }
        if (status != OrderStatus.CREATED && status != OrderStatus.PAYMENT_PENDING) {
            throw new InvalidOrderStateException(status, OrderStatus.CANCELLED);
        }
        cancelAll(reason, OrderStatus.CANCELLED, false);
    }

    /** Expires an unpaid order after the payment window. */
    public void expireUnpaid(LocalDateTime cutoff) {
        Objects.requireNonNull(cutoff, "cutoff");
        if (status == OrderStatus.EXPIRED) {
            return;
        }
        if (status != OrderStatus.CREATED && status != OrderStatus.PAYMENT_PENDING) {
            throw new InvalidOrderStateException(status, OrderStatus.EXPIRED);
        }
        if (createdAt.isAfter(cutoff)) {
            throw new InvalidOrderValidationException("Order is not old enough to expire");
        }
        cancelAll(CancellationReason.PAYMENT_TIMEOUT, OrderStatus.EXPIRED, true);
    }

    /** Cancels one seller order without resurrecting it during later parent transitions. */
    public void cancelSellerOrder(Long sellerId, CancellationReason reason) {
        Objects.requireNonNull(sellerId, "sellerId");
        Objects.requireNonNull(reason, "reason");
        if (status == OrderStatus.FULFILLING
                || status == OrderStatus.COMPLETED
                || status == OrderStatus.CANCELLED
                || status == OrderStatus.EXPIRED) {
            throw new InvalidOrderStateException(status, OrderStatus.CANCELLED);
        }
        boolean found = false;
        for (int index = 0; index < sellerOrders.size(); index++) {
            SellerOrder sellerOrder = sellerOrders.get(index);
            if (sellerId.equals(sellerOrder.sellerId())) {
                found = true;
                sellerOrders.set(index, sellerOrder.cancel());
                break;
            }
        }
        if (!found) {
            throw new InvalidOrderValidationException("Seller order not found: " + sellerId);
        }
        if (sellerOrders.stream().allMatch(SellerOrder::isTerminalCancellation)) {
            status = OrderStatus.CANCELLED;
            cancellationReason = reason;
        }
        updatedAt = LocalDateTime.now();
    }

    /**
     * Guarded operations override for exceptional recovery. Paid/fulfilling orders require proof
     * that payment/inventory/fulfillment compensation has already been coordinated.
     */
    public void manualOverrideCancel(
            CancellationReason reason, boolean downstreamCompensationConfirmed) {
        Objects.requireNonNull(reason, "reason");
        if (status == OrderStatus.COMPLETED) {
            throw new InvalidOrderStateException(status, OrderStatus.CANCELLED);
        }
        if ((status == OrderStatus.PAID || status == OrderStatus.FULFILLING)
                && !downstreamCompensationConfirmed) {
            throw new InvalidOrderValidationException(
                    "Paid/fulfilling order override requires downstream compensation confirmation");
        }
        if (status == OrderStatus.PAID || status == OrderStatus.FULFILLING) {
            status = OrderStatus.CANCELLED;
            cancellationReason = reason;
            updatedAt = LocalDateTime.now();
            sellerOrders.replaceAll(SellerOrder::forceCancel);
            return;
        }
        cancelAll(reason, OrderStatus.CANCELLED, false);
    }

    private void cancelAll(CancellationReason reason, OrderStatus target, boolean expired) {
        status = target;
        cancellationReason = reason;
        updatedAt = LocalDateTime.now();
        sellerOrders.replaceAll(expired ? SellerOrder::expire : SellerOrder::cancel);
    }

    private void transition(OrderStatus expected, OrderStatus target) {
        if (status != expected) {
            throw new InvalidOrderStateException(status, target);
        }
        status = target;
        updatedAt = LocalDateTime.now();
    }

    private static void validateMoney(
            List<LineCommand> lines, BigDecimal grossAmount, BigDecimal discountAmount) {
        if (lines == null || lines.isEmpty()) {
            throw new InvalidOrderValidationException("Order lines are required");
        }
        if (grossAmount == null || grossAmount.signum() <= 0) {
            throw new InvalidOrderValidationException("Gross amount must be positive");
        }
        if (discountAmount == null
                || discountAmount.signum() < 0
                || discountAmount.compareTo(grossAmount) > 0) {
            throw new InvalidOrderValidationException("Discount amount is invalid");
        }
        BigDecimal calculatedGross =
                lines.stream().map(LineCommand::gross).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (calculatedGross.compareTo(grossAmount) != 0) {
            throw new InvalidOrderValidationException("Gross amount does not match line snapshot");
        }
    }

    private static List<SellerOrder> allocateSellerOrders(
            String orderNo,
            List<LineCommand> lines,
            BigDecimal grossAmount,
            BigDecimal discountAmount) {
        Map<Long, List<LineCommand>> groupedLines = new LinkedHashMap<>();
        lines.forEach(
                line ->
                        groupedLines
                                .computeIfAbsent(line.sellerId(), ignored -> new ArrayList<>())
                                .add(line));

        List<SellerOrder> sellerOrders = new ArrayList<>();
        BigDecimal allocatedSellerDiscount = BigDecimal.ZERO;
        int sellerIndex = 0;
        for (Map.Entry<Long, List<LineCommand>> entry : groupedLines.entrySet()) {
            sellerIndex++;
            BigDecimal sellerGross =
                    entry.getValue().stream()
                            .map(LineCommand::gross)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal sellerDiscount =
                    sellerIndex == groupedLines.size()
                            ? discountAmount.subtract(allocatedSellerDiscount)
                            : discountAmount
                                    .multiply(sellerGross)
                                    .divide(grossAmount, 2, RoundingMode.HALF_UP);
            allocatedSellerDiscount = allocatedSellerDiscount.add(sellerDiscount);

            List<OrderLine> allocatedLines =
                    allocateLines(entry.getValue(), sellerGross, sellerDiscount);
            sellerOrders.add(
                    new SellerOrder(
                            null,
                            entry.getKey(),
                            orderNo + "-S" + entry.getKey(),
                            sellerGross,
                            sellerDiscount,
                            sellerGross.subtract(sellerDiscount),
                            SellerOrderStatus.CREATED,
                            allocatedLines));
        }
        return List.copyOf(sellerOrders);
    }

    private static List<OrderLine> allocateLines(
            List<LineCommand> lines, BigDecimal sellerGross, BigDecimal sellerDiscount) {
        List<OrderLine> allocatedLines = new ArrayList<>();
        BigDecimal allocatedDiscount = BigDecimal.ZERO;
        for (int index = 0; index < lines.size(); index++) {
            LineCommand line = lines.get(index);
            BigDecimal lineDiscount =
                    index == lines.size() - 1
                            ? sellerDiscount.subtract(allocatedDiscount)
                            : sellerDiscount
                                    .multiply(line.gross())
                                    .divide(sellerGross, 2, RoundingMode.HALF_UP);
            allocatedDiscount = allocatedDiscount.add(lineDiscount);
            allocatedLines.add(
                    new OrderLine(
                            null,
                            line.skuId(),
                            line.quantity(),
                            line.unitPrice(),
                            lineDiscount,
                            line.gross().subtract(lineDiscount)));
        }
        return List.copyOf(allocatedLines);
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new InvalidOrderValidationException(field + " is required");
        }
        return value;
    }

    /** Incoming immutable checkout line. */
    public record LineCommand(Long sellerId, Long skuId, int quantity, BigDecimal unitPrice) {
        public LineCommand {
            if (sellerId == null
                    || skuId == null
                    || quantity <= 0
                    || unitPrice == null
                    || unitPrice.signum() <= 0) {
                throw new InvalidOrderValidationException("Order line is invalid");
            }
        }

        /** Returns line gross amount. */
        public BigDecimal gross() {
            return unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }

    /** Immutable persisted order-line monetary snapshot. */
    public record OrderLine(
            Long id,
            Long skuId,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal allocatedDiscount,
            BigDecimal netAmount) {}

    /** Immutable seller child-order snapshot. */
    public record SellerOrder(
            Long id,
            Long sellerId,
            String sellerOrderNo,
            BigDecimal grossAmount,
            BigDecimal discountAmount,
            BigDecimal payableAmount,
            SellerOrderStatus status,
            List<OrderLine> lines) {

        /** Returns a copy in paid state. */
        public SellerOrder markPaid() {
            return isTerminalCancellation() ? this : withStatus(SellerOrderStatus.PAID);
        }

        /** Returns a copy in fulfillment state. */
        public SellerOrder markFulfilling() {
            return isTerminalCancellation() ? this : withStatus(SellerOrderStatus.FULFILLING);
        }

        /** Returns a copy in completed state. */
        public SellerOrder markCompleted() {
            return isTerminalCancellation() ? this : withStatus(SellerOrderStatus.COMPLETED);
        }

        /** Returns a copy in cancelled state. */
        public SellerOrder cancel() {
            if (status == SellerOrderStatus.COMPLETED || status == SellerOrderStatus.FULFILLING) {
                throw new InvalidOrderValidationException(
                        "Seller order cannot be cancelled from " + status);
            }
            return isTerminalCancellation() ? this : withStatus(SellerOrderStatus.CANCELLED);
        }

        /** Operations-only copy used after downstream compensation has been confirmed. */
        public SellerOrder forceCancel() {
            return isTerminalCancellation() ? this : withStatus(SellerOrderStatus.CANCELLED);
        }

        /** Returns a copy expired by unpaid-order timeout. */
        public SellerOrder expire() {
            return isTerminalCancellation() ? this : withStatus(SellerOrderStatus.EXPIRED);
        }

        /** Whether this seller order must not be advanced by parent lifecycle transitions. */
        public boolean isTerminalCancellation() {
            return status == SellerOrderStatus.CANCELLED || status == SellerOrderStatus.EXPIRED;
        }

        private SellerOrder withStatus(SellerOrderStatus targetStatus) {
            return new SellerOrder(
                    id,
                    sellerId,
                    sellerOrderNo,
                    grossAmount,
                    discountAmount,
                    payableAmount,
                    targetStatus,
                    lines);
        }
    }

    public String orderNo() {
        return orderNo;
    }

    public String checkoutKey() {
        return checkoutKey;
    }

    public String userId() {
        return userId;
    }

    public BigDecimal grossAmount() {
        return grossAmount;
    }

    public BigDecimal discountAmount() {
        return discountAmount;
    }

    public BigDecimal payableAmount() {
        return payableAmount;
    }

    public List<SellerOrder> sellerOrders() {
        return List.copyOf(sellerOrders);
    }

    /** JavaBean accessor used by mapping frameworks without exposing the mutable backing list. */
    public List<SellerOrder> getSellerOrders() {
        return List.copyOf(sellerOrders);
    }

    public OrderStatus status() {
        return status;
    }

    public CancellationReason cancellationReason() {
        return cancellationReason;
    }

    public LocalDateTime createdAt() {
        return createdAt;
    }

    public LocalDateTime updatedAt() {
        return updatedAt;
    }

    public long version() {
        return version;
    }
}
