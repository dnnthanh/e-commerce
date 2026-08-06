package com.dnnthanh.marketplace.be.returns.api.adapter.out.persistence.entity;

import com.dnnthanh.marketplace.be.returns.api.domain.enumtype.ReturnStatus;
import com.dnnthanh.marketplace.be.returns.api.domain.model.ReturnRequest;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** JPA representation of the canonical customer return aggregate. */
@Entity
@Table(name = "return_request")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReturnRequestJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "return_key", nullable = false, unique = true, length = 128)
    private String returnKey;

    @Column(name = "order_id", nullable = false, length = 64)
    private String orderId;

    @Column(name = "user_id", nullable = false, length = 64)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ReturnStatus status;

    @Column(nullable = false, length = 255)
    private String reason;

    @Column(name = "refundable_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal refundableAmount;

    @Column(name = "receiving_warehouse_id")
    private Long receivingWarehouseId;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "dispute_reason", length = 500)
    private String disputeReason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    @Column(nullable = false)
    private long version;

    @OneToMany(
            mappedBy = "request",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<ReturnLineJpaEntity> lines = new ArrayList<>();

    public ReturnRequestJpaEntity(ReturnRequest aggregate) {
        this.returnKey = aggregate.key();
        this.orderId = aggregate.orderId();
        this.userId = aggregate.userId();
        this.reason = aggregate.reason();
        this.createdAt = aggregate.createdAt();
        apply(aggregate);
    }

    /** Applies mutable workflow state while preserving immutable request identity/snapshots. */
    public void apply(ReturnRequest aggregate) {
        this.status = aggregate.status();
        this.refundableAmount = aggregate.refundableAmount();
        this.receivingWarehouseId = aggregate.receivingWarehouseId();
        this.rejectionReason = aggregate.rejectionReason();
        this.disputeReason = aggregate.disputeReason();
        this.updatedAt = LocalDateTime.now();
        if (lines.isEmpty()) {
            replaceLines(
                    aggregate.lines().stream()
                            .map(
                                    line ->
                                            new ReturnLineJpaEntity(
                                                    line.orderLineId(),
                                                    line.sellerId(),
                                                    line.skuId(),
                                                    line.requestedQuantity(),
                                                    line.orderedQuantity(),
                                                    line.alreadyReturnedQuantity(),
                                                    line.refundableUnitAmount(),
                                                    line.requestedRefundAmount(),
                                                    line.deliveredAt(),
                                                    line.acceptedQuantity(),
                                                    line.disposition()))
                            .toList());
            return;
        }
        for (ReturnLineJpaEntity entityLine : lines) {
            aggregate.lines().stream()
                    .filter(line -> line.orderLineId().equals(entityLine.getOrderLineId()))
                    .findFirst()
                    .ifPresent(
                            line ->
                                    entityLine.applyInspection(
                                            line.acceptedQuantity(), line.disposition()));
        }
    }

    private void replaceLines(List<ReturnLineJpaEntity> children) {
        lines.clear();
        children.forEach(
                child -> {
                    child.attachTo(this);
                    lines.add(child);
                });
    }
}
