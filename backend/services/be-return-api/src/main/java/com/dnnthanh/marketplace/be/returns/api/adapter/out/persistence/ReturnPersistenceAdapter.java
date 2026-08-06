package com.dnnthanh.marketplace.be.returns.api.adapter.out.persistence;

import com.dnnthanh.marketplace.be.platform.stereotype.Persistence;
import com.dnnthanh.marketplace.be.returns.api.adapter.out.persistence.entity.ReturnOutboxJpaEntity;
import com.dnnthanh.marketplace.be.returns.api.adapter.out.persistence.entity.ReturnRequestJpaEntity;
import com.dnnthanh.marketplace.be.returns.api.adapter.out.persistence.repository.ReturnLineJpaRepository;
import com.dnnthanh.marketplace.be.returns.api.adapter.out.persistence.repository.ReturnOutboxJpaRepository;
import com.dnnthanh.marketplace.be.returns.api.adapter.out.persistence.repository.ReturnRequestJpaRepository;
import com.dnnthanh.marketplace.be.returns.api.application.exception.ConcurrentReturnCreateException;
import com.dnnthanh.marketplace.be.returns.api.application.port.out.ReturnQueryPort;
import com.dnnthanh.marketplace.be.returns.api.application.port.out.ReturnRepositoryPort;
import com.dnnthanh.marketplace.be.returns.api.application.query.ReturnQueryResult;
import com.dnnthanh.marketplace.be.returns.api.domain.enumtype.InventoryDisposition;
import com.dnnthanh.marketplace.be.returns.api.domain.enumtype.ReturnEventType;
import com.dnnthanh.marketplace.be.returns.api.domain.enumtype.ReturnStatus;
import com.dnnthanh.marketplace.be.returns.api.domain.exception.InvalidReturnException;
import com.dnnthanh.marketplace.be.returns.api.domain.model.ReturnRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

/** JPA adapter for canonical Return aggregate persistence and read models. */
@Persistence
@RequiredArgsConstructor
public class ReturnPersistenceAdapter implements ReturnRepositoryPort, ReturnQueryPort {
    private static final String OUTBOX_PENDING = "PENDING";

    private final ReturnRequestJpaRepository requestRepository;
    private final ReturnLineJpaRepository lineRepository;
    private final ReturnOutboxJpaRepository outboxRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<ReturnRequest> loadByKey(String returnKey) {
        return requestRepository.findByReturnKey(returnKey).map(ReturnPersistenceAdapter::toDomain);
    }

    @Override
    @Transactional
    public ReturnRequest save(ReturnRequest request) {
        Optional<ReturnRequestJpaEntity> existing =
                requestRepository.findByReturnKey(request.key());
        if (existing.isEmpty()) {
            ReturnRequestJpaEntity created = new ReturnRequestJpaEntity(request);
            try {
                requestRepository.saveAndFlush(created);
            } catch (DataIntegrityViolationException duplicate) {
                throw new ConcurrentReturnCreateException(duplicate);
            }
            emit(request, ReturnEventType.RETURN_REQUESTED);
            return request;
        }

        ReturnRequestJpaEntity entity = existing.orElseThrow();
        ReturnStatus before = entity.getStatus();
        entity.apply(request);
        requestRepository.saveAndFlush(entity);
        if (before != request.status()) {
            emit(request, eventFor(request.status()));
            if (before != ReturnStatus.INSPECTED && request.status() == ReturnStatus.INSPECTED) {
                emitDispositionEvents(request);
            }
        }
        return request;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ReturnQueryResult> findByKey(String returnKey) {
        return requestRepository
                .findByReturnKey(returnKey)
                .map(ReturnPersistenceAdapter::toQueryResult);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReturnQueryResult> findByUser(String userId, int limit) {
        return requestRepository.findRecentByUserNative(userId).stream()
                .limit(limit)
                .map(ReturnPersistenceAdapter::toQueryResult)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean containsSellerLine(String returnKey, Long sellerId) {
        return lineRepository.existsByRequest_ReturnKeyAndSellerId(returnKey, sellerId);
    }

    @Override
    @Transactional(readOnly = true)
    public int activeReturnedQuantity(String orderId, Long orderLineId) {
        return Math.toIntExact(lineRepository.activeReturnedQuantity(orderId, orderLineId));
    }

    private void emit(ReturnRequest request, ReturnEventType eventType) {
        outboxRepository.save(
                new ReturnOutboxJpaEntity(
                        UUID.randomUUID().toString(),
                        request.key(),
                        eventType.name(),
                        Map.of(
                                "returnKey", request.key(),
                                "orderId", request.orderId(),
                                "status", request.status().name(),
                                "refundableAmount", request.refundableAmount()),
                        OUTBOX_PENDING,
                        LocalDateTime.now()));
    }

    private void emitDispositionEvents(ReturnRequest request) {
        if (request.status() != ReturnStatus.INSPECTED) {
            return;
        }
        request.lines().stream()
                .filter(line -> line.acceptedQuantity() > 0)
                .filter(line -> line.disposition() != InventoryDisposition.NONE)
                .forEach(
                        line ->
                                outboxRepository.save(
                                        new ReturnOutboxJpaEntity(
                                                UUID.randomUUID().toString(),
                                                request.key(),
                                                dispositionEvent(line.disposition()).name(),
                                                Map.of(
                                                        "returnKey", request.key(),
                                                        "orderLineId", line.orderLineId(),
                                                        "skuId", line.skuId(),
                                                        "quantity", line.acceptedQuantity(),
                                                        "warehouseId",
                                                                request.receivingWarehouseId()),
                                                OUTBOX_PENDING,
                                                LocalDateTime.now())));
    }

    private static ReturnEventType dispositionEvent(InventoryDisposition disposition) {
        return switch (disposition) {
            case RESTOCK -> ReturnEventType.RETURN_ITEM_RESTOCK_REQUESTED;
            case QUARANTINE -> ReturnEventType.RETURN_ITEM_QUARANTINE_REQUESTED;
            case SCRAP -> ReturnEventType.RETURN_ITEM_SCRAP_REQUESTED;
            case NONE ->
                    throw new InvalidReturnException(
                            "NONE disposition must not emit inventory event");
        };
    }

    private static ReturnEventType eventFor(ReturnStatus status) {
        return switch (status) {
            case APPROVED -> ReturnEventType.RETURN_APPROVED;
            case REJECTED -> ReturnEventType.RETURN_REJECTED;
            case RECEIVED -> ReturnEventType.RETURN_RECEIVED;
            case INSPECTED -> ReturnEventType.RETURN_INSPECTED;
            case DISPUTED -> ReturnEventType.RETURN_DISPUTED;
            case COMPLETED -> ReturnEventType.RETURN_COMPLETED;
            case REFUND_PENDING -> ReturnEventType.RETURN_REFUND_REQUESTED;
            case REFUND_UNKNOWN -> ReturnEventType.RETURN_REFUND_UNKNOWN;
            case REFUND_FAILED -> ReturnEventType.RETURN_REFUND_FAILED;
            default -> ReturnEventType.RETURN_DISPUTE_RESOLVED;
        };
    }

    private static ReturnQueryResult toQueryResult(ReturnRequestJpaEntity entity) {
        return new ReturnQueryResult(
                entity.getReturnKey(),
                entity.getOrderId(),
                entity.getStatus(),
                entity.getRefundableAmount(),
                entity.getReceivingWarehouseId(),
                entity.getCreatedAt());
    }

    private static ReturnRequest toDomain(ReturnRequestJpaEntity entity) {
        return ReturnRequest.rehydrate(
                entity.getReturnKey(),
                entity.getOrderId(),
                entity.getUserId(),
                entity.getReason(),
                entity.getLines().stream()
                        .map(
                                line ->
                                        ReturnRequest.ReturnLine.rehydrate(
                                                line.getOrderLineId(),
                                                line.getSellerId(),
                                                line.getSkuId(),
                                                line.getOrderedQuantity(),
                                                line.getAlreadyReturnedQuantity(),
                                                line.getQuantity(),
                                                line.getRefundableUnitAmount(),
                                                line.getDeliveredAt(),
                                                line.getAcceptedQuantity(),
                                                line.getInventoryDisposition()))
                        .toList(),
                entity.getStatus(),
                entity.getRefundableAmount(),
                entity.getReceivingWarehouseId(),
                entity.getRejectionReason(),
                entity.getDisputeReason(),
                entity.getCreatedAt());
    }
}
