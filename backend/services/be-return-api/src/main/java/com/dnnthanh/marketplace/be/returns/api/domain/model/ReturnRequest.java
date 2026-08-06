package com.dnnthanh.marketplace.be.returns.api.domain.model;

import com.dnnthanh.marketplace.be.returns.api.domain.enumtype.InventoryDisposition;
import com.dnnthanh.marketplace.be.returns.api.domain.enumtype.ReturnStatus;
import com.dnnthanh.marketplace.be.returns.api.domain.exception.InvalidReturnException;
import com.dnnthanh.marketplace.be.returns.api.domain.exception.ReturnStateConflictException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Canonical customer return aggregate.
 *
 * <p>The aggregate owns requested quantity, physical inspection, dispute and refund eligibility. It
 * intentionally keeps immutable Order facts next to mutable inspection decisions so later price or
 * order changes cannot alter the refundable amount.
 */
public final class ReturnRequest {
    private final String key;
    private final String orderId;
    private final String userId;
    private final String reason;
    private final List<ReturnLine> lines;
    private ReturnStatus status;
    private BigDecimal refundableAmount;
    private Long receivingWarehouseId;
    private String rejectionReason;
    private String disputeReason;
    private final LocalDateTime createdAt;

    public ReturnRequest(
            String key, String orderId, String userId, String reason, List<ReturnLine> lines) {
        this(
                key,
                orderId,
                userId,
                reason,
                lines,
                ReturnStatus.REQUESTED,
                requestedRefund(lines),
                null,
                null,
                null,
                LocalDateTime.now());
    }

    private ReturnRequest(
            String key,
            String orderId,
            String userId,
            String reason,
            List<ReturnLine> lines,
            ReturnStatus status,
            BigDecimal refundableAmount,
            Long receivingWarehouseId,
            String rejectionReason,
            String disputeReason,
            LocalDateTime createdAt) {
        if (lines == null || lines.isEmpty()) {
            throw new InvalidReturnException("return lines are required");
        }
        this.key = requireText(key, "return key is required");
        this.orderId = requireText(orderId, "order id is required");
        this.userId = requireText(userId, "user id is required");
        this.reason = requireText(reason, "return reason is required");
        this.lines = new ArrayList<>(lines);
        this.status = Objects.requireNonNull(status, "status");
        this.refundableAmount = normalizeMoney(refundableAmount);
        this.receivingWarehouseId = receivingWarehouseId;
        this.rejectionReason = rejectionReason;
        this.disputeReason = disputeReason;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
    }

    /** Rehydrates persisted state without re-running workflow transitions. */
    public static ReturnRequest rehydrate(
            String key,
            String orderId,
            String userId,
            String reason,
            List<ReturnLine> lines,
            ReturnStatus status,
            BigDecimal refundableAmount,
            Long receivingWarehouseId,
            String rejectionReason,
            String disputeReason,
            LocalDateTime createdAt) {
        return new ReturnRequest(
                key,
                orderId,
                userId,
                reason,
                lines,
                status,
                refundableAmount,
                receivingWarehouseId,
                rejectionReason,
                disputeReason,
                createdAt);
    }

    /** Seller accepts the return request. */
    public void approve() {
        transition(ReturnStatus.REQUESTED, ReturnStatus.APPROVED);
        rejectionReason = null;
    }

    /** Seller rejects a request with an auditable reason. */
    public void reject(String reason) {
        if (status != ReturnStatus.REQUESTED && status != ReturnStatus.DISPUTED) {
            throw conflict(ReturnStatus.REJECTED);
        }
        rejectionReason = requireText(reason, "rejection reason is required");
        status = ReturnStatus.REJECTED;
    }

    /** Warehouse receives the physical return before inspection. */
    public void receive(Long warehouseId) {
        if (status != ReturnStatus.APPROVED && status != ReturnStatus.IN_TRANSIT) {
            throw conflict(ReturnStatus.RECEIVED);
        }
        if (warehouseId == null || warehouseId <= 0) {
            throw new InvalidReturnException("receiving warehouse is required");
        }
        receivingWarehouseId = warehouseId;
        status = ReturnStatus.RECEIVED;
    }

    /**
     * Applies a full physical inspection and derives the exact refundable amount. Every line must
     * be inspected exactly once to avoid silently ignoring damaged items.
     */
    public BigDecimal inspect(Map<Long, InspectionDecision> decisions) {
        if (status != ReturnStatus.RECEIVED && status != ReturnStatus.DISPUTED) {
            throw conflict(ReturnStatus.INSPECTED);
        }
        if (decisions == null || decisions.size() != lines.size()) {
            throw new InvalidReturnException(
                    "inspection must contain every return line exactly once");
        }
        Map<Long, ReturnLine> byId = new LinkedHashMap<>();
        lines.forEach(line -> byId.put(line.orderLineId(), line));
        if (!byId.keySet().equals(decisions.keySet())) {
            throw new InvalidReturnException("inspection line ids do not match the return request");
        }

        BigDecimal approved = BigDecimal.ZERO;
        for (Map.Entry<Long, InspectionDecision> entry : decisions.entrySet()) {
            ReturnLine line = byId.get(entry.getKey());
            line.inspect(entry.getValue());
            approved = approved.add(line.approvedRefundAmount());
        }
        refundableAmount = normalizeMoney(approved);
        status = ReturnStatus.INSPECTED;
        disputeReason = null;
        return refundableAmount;
    }

    /** Customer opens a dispute after rejection or inspection. */
    public void openDispute(String reason) {
        if (status != ReturnStatus.REJECTED && status != ReturnStatus.INSPECTED) {
            throw conflict(ReturnStatus.DISPUTED);
        }
        disputeReason = requireText(reason, "dispute reason is required");
        status = ReturnStatus.DISPUTED;
    }

    /** Operations resolves a dispute either by accepting a new inspection or rejecting it. */
    public void resolveDispute(
            boolean accepted, Map<Long, InspectionDecision> decisions, String reason) {
        if (status != ReturnStatus.DISPUTED) {
            throw conflict(accepted ? ReturnStatus.INSPECTED : ReturnStatus.REJECTED);
        }
        if (!accepted) {
            reject(reason);
            disputeReason = null;
            return;
        }
        inspect(decisions);
    }

    /** Moves an inspected/retryable return into the idempotent refund-attempt state. */
    public void prepareRefund() {
        if (status == ReturnStatus.REFUND_PENDING) {
            return;
        }
        if (!status.canPrepareRefund()) {
            throw conflict(ReturnStatus.REFUND_PENDING);
        }
        if (refundableAmount.signum() <= 0) {
            throw new InvalidReturnException("return has no accepted quantity to refund");
        }
        status = ReturnStatus.REFUND_PENDING;
    }

    public void markRefundUnknown() {
        transition(ReturnStatus.REFUND_PENDING, ReturnStatus.REFUND_UNKNOWN);
    }

    public void markRefundFailed() {
        transition(ReturnStatus.REFUND_PENDING, ReturnStatus.REFUND_FAILED);
    }

    /** Durable payment-refunded event finalizes the workflow. */
    public void completeRefund(BigDecimal actualAmount) {
        if (status != ReturnStatus.REFUND_PENDING
                && status != ReturnStatus.REFUND_UNKNOWN
                && status != ReturnStatus.REFUNDED) {
            throw conflict(ReturnStatus.COMPLETED);
        }
        if (normalizeMoney(actualAmount).compareTo(refundableAmount) != 0) {
            throw new InvalidReturnException(
                    "actual refund does not match inspected refundable amount");
        }
        status = ReturnStatus.COMPLETED;
    }

    private void transition(ReturnStatus expected, ReturnStatus target) {
        if (status != expected) {
            throw conflict(target);
        }
        status = target;
    }

    private ReturnStateConflictException conflict(ReturnStatus target) {
        return new ReturnStateConflictException(
                "Return cannot transition from %s to %s".formatted(status, target));
    }

    private static BigDecimal requestedRefund(List<ReturnLine> lines) {
        if (lines == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return normalizeMoney(
                lines.stream()
                        .map(ReturnLine::requestedRefundAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    private static BigDecimal normalizeMoney(BigDecimal value) {
        return Objects.requireNonNull(value, "money").setScale(2, RoundingMode.HALF_UP);
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new InvalidReturnException(message);
        }
        return value;
    }

    public String key() {
        return key;
    }

    public String orderId() {
        return orderId;
    }

    public String userId() {
        return userId;
    }

    public String reason() {
        return reason;
    }

    public List<ReturnLine> lines() {
        return List.copyOf(lines);
    }

    public BigDecimal refundableAmount() {
        return refundableAmount;
    }

    public ReturnStatus status() {
        return status;
    }

    public Long receivingWarehouseId() {
        return receivingWarehouseId;
    }

    public String rejectionReason() {
        return rejectionReason;
    }

    public String disputeReason() {
        return disputeReason;
    }

    public LocalDateTime createdAt() {
        return createdAt;
    }

    /** Immutable order facts plus mutable physical inspection decision. */
    public static final class ReturnLine {
        private final Long orderLineId;
        private final Long sellerId;
        private final Long skuId;
        private final int orderedQuantity;
        private final int alreadyReturnedQuantity;
        private final int requestedQuantity;
        private final BigDecimal refundableUnitAmount;
        private final LocalDateTime deliveredAt;
        private int acceptedQuantity;
        private InventoryDisposition disposition;

        public ReturnLine(
                Long orderLineId,
                Long sellerId,
                Long skuId,
                int orderedQuantity,
                int alreadyReturnedQuantity,
                int requestedQuantity,
                BigDecimal refundableUnitAmount,
                LocalDateTime deliveredAt) {
            this(
                    orderLineId,
                    sellerId,
                    skuId,
                    orderedQuantity,
                    alreadyReturnedQuantity,
                    requestedQuantity,
                    refundableUnitAmount,
                    deliveredAt,
                    0,
                    InventoryDisposition.NONE);
        }

        private ReturnLine(
                Long orderLineId,
                Long sellerId,
                Long skuId,
                int orderedQuantity,
                int alreadyReturnedQuantity,
                int requestedQuantity,
                BigDecimal refundableUnitAmount,
                LocalDateTime deliveredAt,
                int acceptedQuantity,
                InventoryDisposition disposition) {
            this.orderLineId = Objects.requireNonNull(orderLineId, "orderLineId");
            this.sellerId = Objects.requireNonNull(sellerId, "sellerId");
            this.skuId = Objects.requireNonNull(skuId, "skuId");
            if (orderedQuantity <= 0
                    || alreadyReturnedQuantity < 0
                    || requestedQuantity <= 0
                    || requestedQuantity + alreadyReturnedQuantity > orderedQuantity) {
                throw new InvalidReturnException(
                        "return quantity exceeds remaining delivered quantity");
            }
            this.orderedQuantity = orderedQuantity;
            this.alreadyReturnedQuantity = alreadyReturnedQuantity;
            this.requestedQuantity = requestedQuantity;
            this.refundableUnitAmount = normalizeMoney(refundableUnitAmount);
            this.deliveredAt = Objects.requireNonNull(deliveredAt, "deliveredAt");
            if (acceptedQuantity < 0 || acceptedQuantity > requestedQuantity) {
                throw new InvalidReturnException("accepted quantity exceeds requested quantity");
            }
            this.acceptedQuantity = acceptedQuantity;
            this.disposition = Objects.requireNonNull(disposition, "disposition");
        }

        /** Rehydrates a line including persisted inspection outcome. */
        public static ReturnLine rehydrate(
                Long orderLineId,
                Long sellerId,
                Long skuId,
                int orderedQuantity,
                int alreadyReturnedQuantity,
                int requestedQuantity,
                BigDecimal refundableUnitAmount,
                LocalDateTime deliveredAt,
                int acceptedQuantity,
                InventoryDisposition disposition) {
            return new ReturnLine(
                    orderLineId,
                    sellerId,
                    skuId,
                    orderedQuantity,
                    alreadyReturnedQuantity,
                    requestedQuantity,
                    refundableUnitAmount,
                    deliveredAt,
                    acceptedQuantity,
                    disposition);
        }

        private void inspect(InspectionDecision decision) {
            Objects.requireNonNull(decision, "inspection decision");
            if (decision.acceptedQuantity() < 0
                    || decision.acceptedQuantity() > requestedQuantity) {
                throw new InvalidReturnException("accepted quantity exceeds requested quantity");
            }
            if (decision.acceptedQuantity() > 0
                    && decision.disposition() == InventoryDisposition.NONE) {
                throw new InvalidReturnException(
                        "accepted return quantity requires inventory disposition");
            }
            acceptedQuantity = decision.acceptedQuantity();
            disposition = decision.disposition();
        }

        public BigDecimal requestedRefundAmount() {
            return normalizeMoney(
                    refundableUnitAmount.multiply(BigDecimal.valueOf(requestedQuantity)));
        }

        public BigDecimal approvedRefundAmount() {
            return normalizeMoney(
                    refundableUnitAmount.multiply(BigDecimal.valueOf(acceptedQuantity)));
        }

        public Long orderLineId() {
            return orderLineId;
        }

        public Long sellerId() {
            return sellerId;
        }

        public Long skuId() {
            return skuId;
        }

        public int orderedQuantity() {
            return orderedQuantity;
        }

        public int alreadyReturnedQuantity() {
            return alreadyReturnedQuantity;
        }

        public int requestedQuantity() {
            return requestedQuantity;
        }

        public BigDecimal refundableUnitAmount() {
            return refundableUnitAmount;
        }

        public LocalDateTime deliveredAt() {
            return deliveredAt;
        }

        public int acceptedQuantity() {
            return acceptedQuantity;
        }

        public InventoryDisposition disposition() {
            return disposition;
        }
    }

    /** Physical inspection decision for one return line. */
    public record InspectionDecision(int acceptedQuantity, InventoryDisposition disposition) {
        public InspectionDecision {
            if (acceptedQuantity < 0) {
                throw new InvalidReturnException("accepted quantity cannot be negative");
            }
            Objects.requireNonNull(disposition, "disposition");
        }
    }
}
