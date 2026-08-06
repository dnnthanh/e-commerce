package com.dnnthanh.marketplace.be.checkout.api.domain.model;

import com.dnnthanh.marketplace.be.checkout.api.domain.enumtype.CheckoutStep;
import com.dnnthanh.marketplace.be.checkout.api.domain.exception.CheckoutStateConflictException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Persistent Saga/process-manager state with explicit compensation requirements. */
public final class CheckoutProcess {
    private final String checkoutId;
    private final String idempotencyKey;
    private CheckoutStep step = CheckoutStep.CREATED;
    private boolean promotionReserved;
    private boolean inventoryReserved;
    private boolean paymentInitiated;
    private String orderId;
    private final List<String> completedActions = new ArrayList<>();

    public CheckoutProcess(String checkoutId, String idempotencyKey) {
        this.checkoutId = Objects.requireNonNull(checkoutId);
        this.idempotencyKey = Objects.requireNonNull(idempotencyKey);
    }

    public static CheckoutProcess rehydrate(
            String checkoutId,
            String idempotencyKey,
            CheckoutStep step,
            boolean promotionReserved,
            boolean inventoryReserved,
            boolean paymentInitiated,
            String orderId,
            List<String> completedActions) {
        CheckoutProcess process = new CheckoutProcess(checkoutId, idempotencyKey);
        process.step = Objects.requireNonNull(step);
        process.promotionReserved = promotionReserved;
        process.inventoryReserved = inventoryReserved;
        process.paymentInitiated = paymentInitiated;
        process.orderId = orderId;
        process.completedActions.clear();
        process.completedActions.addAll(completedActions == null ? List.of() : completedActions);
        return process;
    }

    public void quoted() {
        advance(CheckoutStep.CREATED, CheckoutStep.QUOTED, "PRICE_QUOTED");
    }

    public void promotionReserved() {
        advance(CheckoutStep.QUOTED, CheckoutStep.PROMOTION_RESERVED, "PROMOTION_RESERVED");
        promotionReserved = true;
    }

    public void inventoryReserved() {
        advance(
                CheckoutStep.PROMOTION_RESERVED,
                CheckoutStep.INVENTORY_RESERVED,
                "INVENTORY_RESERVED");
        inventoryReserved = true;
    }

    public void paymentInitiated() {
        advance(CheckoutStep.INVENTORY_RESERVED, CheckoutStep.PAYMENT_PENDING, "PAYMENT_INITIATED");
        paymentInitiated = true;
    }

    public void orderCreated(String orderId) {
        if (step != CheckoutStep.INVENTORY_RESERVED && step != CheckoutStep.PAYMENT_PENDING)
            throw new CheckoutStateConflictException("Order cannot be created from " + step);
        this.orderId = Objects.requireNonNull(orderId);
        step = CheckoutStep.ORDER_CREATED;
        completedActions.add("ORDER_CREATED");
    }

    public void complete() {
        advance(CheckoutStep.ORDER_CREATED, CheckoutStep.COMPLETED, "COMPLETED");
    }

    public CompensationPlan fail(String reason) {
        if (step == CheckoutStep.COMPLETED)
            throw new CheckoutStateConflictException("Completed checkout cannot fail");
        step = CheckoutStep.COMPENSATING;
        return new CompensationPlan(inventoryReserved, promotionReserved, paymentInitiated, reason);
    }

    public void compensationCompleted() {
        step = CheckoutStep.FAILED;
    }

    private void advance(CheckoutStep expected, CheckoutStep next, String action) {
        if (step != expected)
            throw new CheckoutStateConflictException(
                    "Checkout step conflict: expected=" + expected + ", actual=" + step);
        step = next;
        completedActions.add(action);
    }

    public CheckoutStep step() {
        return step;
    }

    public String checkoutId() {
        return checkoutId;
    }

    public String idempotencyKey() {
        return idempotencyKey;
    }

    public String orderId() {
        return orderId;
    }

    public boolean promotionReservedFlag() {
        return promotionReserved;
    }

    public boolean inventoryReservedFlag() {
        return inventoryReserved;
    }

    public boolean paymentInitiatedFlag() {
        return paymentInitiated;
    }

    public List<String> completedActions() {
        return List.copyOf(completedActions);
    }

    public record CompensationPlan(
            boolean releaseInventory,
            boolean releasePromotion,
            boolean reconcilePayment,
            String reason) {}
}
