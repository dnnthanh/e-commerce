package com.dnnthanh.marketplace.be.returns.api.application.service;

import static com.dnnthanh.marketplace.be.returns.api.domain.constant.ReturnConstants.MAX_RECENT_RETURNS;
import static com.dnnthanh.marketplace.be.returns.api.domain.constant.ReturnConstants.REFUND_KEY_PREFIX;
import static com.dnnthanh.marketplace.be.returns.api.domain.constant.ReturnConstants.RETURN_KEY_PREFIX;
import static com.dnnthanh.marketplace.be.returns.api.domain.constant.ReturnConstants.RETURN_WINDOW_DAYS;

import com.dnnthanh.marketplace.be.platform.context.UserContext;
import com.dnnthanh.marketplace.be.platform.stereotype.UseCase;
import com.dnnthanh.marketplace.be.returns.api.application.exception.ConcurrentReturnCreateException;
import com.dnnthanh.marketplace.be.returns.api.application.exception.ReturnNotFoundException;
import com.dnnthanh.marketplace.be.returns.api.application.exception.ReturnOrderLineNotFoundException;
import com.dnnthanh.marketplace.be.returns.api.application.exception.ReturnOrderNotFoundException;
import com.dnnthanh.marketplace.be.returns.api.application.port.in.ReturnUseCase;
import com.dnnthanh.marketplace.be.returns.api.application.port.in.ReturnUseCase.CreateReturnCommand;
import com.dnnthanh.marketplace.be.returns.api.application.port.in.ReturnUseCase.InspectLineCommand;
import com.dnnthanh.marketplace.be.returns.api.application.port.in.ReturnUseCase.ReturnResult;
import com.dnnthanh.marketplace.be.returns.api.application.port.out.OrderSnapshotPort;
import com.dnnthanh.marketplace.be.returns.api.application.port.out.OrderSnapshotPort.OrderLineSnapshot;
import com.dnnthanh.marketplace.be.returns.api.application.port.out.RefundPaymentPort;
import com.dnnthanh.marketplace.be.returns.api.application.port.out.ReturnQueryPort;
import com.dnnthanh.marketplace.be.returns.api.application.port.out.ReturnRepositoryPort;
import com.dnnthanh.marketplace.be.returns.api.application.port.out.ReturnWorkflowPersistencePort;
import com.dnnthanh.marketplace.be.returns.api.application.query.ReturnQueryResult;
import com.dnnthanh.marketplace.be.returns.api.domain.enumtype.InventoryDisposition;
import com.dnnthanh.marketplace.be.returns.api.domain.enumtype.ReturnStatus;
import com.dnnthanh.marketplace.be.returns.api.domain.exception.InvalidReturnException;
import com.dnnthanh.marketplace.be.returns.api.domain.model.ReturnRequest;
import com.dnnthanh.marketplace.be.returns.api.domain.model.ReturnRequest.InspectionDecision;
import com.dnnthanh.marketplace.be.returns.api.domain.model.ReturnRequest.ReturnLine;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

/** Return command/query orchestration around one canonical aggregate. */
@UseCase
@RequiredArgsConstructor
public class ReturnServiceImplement implements ReturnUseCase {
    private final OrderSnapshotPort orderSnapshotPort;
    private final RefundPaymentPort refundPaymentPort;
    private final ReturnRepositoryPort returnRepositoryPort;
    private final ReturnQueryPort returnQueryPort;
    private final ReturnWorkflowPersistencePort workflowPersistencePort;
    private final UserContext userContext;
    private final Clock clock;

    /**
     * Creates an idempotent return from immutable Order line snapshots without holding a DB
     * transaction across Order HTTP.
     */
    public ReturnResult create(CreateReturnCommand request) {
        String returnKey = deterministicReturnKey(userContext.userId(), request.requestKey());
        ReturnQueryResult existing = returnQueryPort.findByKey(returnKey).orElse(null);
        if (existing != null) {
            return toResult(existing);
        }

        OrderSnapshotPort.OrderSnapshot order =
                orderSnapshotPort
                        .findByOrderNo(request.orderNo())
                        .filter(snapshot -> userContext.userId().equals(snapshot.userId()))
                        .orElseThrow(ReturnOrderNotFoundException::new);
        validateReturnWindow(order);

        Map<Long, OrderLineSnapshot> orderLinesById = new LinkedHashMap<>();
        order.sellerOrders()
                .forEach(
                        seller ->
                                seller.lines()
                                        .forEach(
                                                line ->
                                                        orderLinesById.put(
                                                                line.orderLineId(), line)));

        List<ReturnLine> selectedLines =
                request.orderLineIds().stream()
                        .distinct()
                        .map(lineId -> toReturnLine(order, orderLinesById.get(lineId)))
                        .toList();
        ReturnRequest aggregate =
                new ReturnRequest(
                        returnKey,
                        order.orderNo(),
                        userContext.userId(),
                        request.reason(),
                        selectedLines);
        try {
            returnRepositoryPort.save(aggregate);
        } catch (ConcurrentReturnCreateException concurrentDuplicate) {
            // Same deterministic key won in another request; return its durable result below.
        }
        return currentView(returnKey);
    }

    public List<ReturnResult> list() {
        return returnQueryPort.findByUser(userContext.userId(), MAX_RECENT_RETURNS).stream()
                .map(ReturnServiceImplement::toResult)
                .toList();
    }

    @Transactional
    public ReturnResult approve(String returnKey, Long sellerId) {
        if (!returnQueryPort.containsSellerLine(returnKey, sellerId)) {
            throw new ReturnNotFoundException();
        }
        ReturnRequest aggregate = requireAggregate(returnKey);
        aggregate.approve();
        returnRepositoryPort.save(aggregate);
        return currentView(returnKey);
    }

    @Transactional
    public ReturnResult reject(String returnKey, Long sellerId, String reason) {
        if (!returnQueryPort.containsSellerLine(returnKey, sellerId)) {
            throw new ReturnNotFoundException();
        }
        ReturnRequest aggregate = requireAggregate(returnKey);
        aggregate.reject(reason);
        returnRepositoryPort.save(aggregate);
        return currentView(returnKey);
    }

    @Transactional
    public ReturnResult receive(String returnKey, Long warehouseId) {
        ReturnRequest aggregate = requireAggregate(returnKey);
        aggregate.receive(warehouseId);
        returnRepositoryPort.save(aggregate);
        return currentView(returnKey);
    }

    @Transactional
    public ReturnResult inspect(String returnKey, List<InspectLineCommand> lines) {
        Map<Long, InspectionDecision> decisions =
                lines.stream()
                        .collect(
                                Collectors.toMap(
                                        InspectLineCommand::orderLineId,
                                        line ->
                                                new InspectionDecision(
                                                        line.acceptedQuantity(),
                                                        InventoryDisposition.valueOf(
                                                                line.disposition())),
                                        (left, right) -> {
                                            throw new InvalidReturnException(
                                                    "duplicate inspection line");
                                        },
                                        LinkedHashMap::new));
        ReturnRequest aggregate = requireAggregate(returnKey);
        aggregate.inspect(decisions);
        returnRepositoryPort.save(aggregate);
        return currentView(returnKey);
    }

    @Transactional
    public ReturnResult openDispute(String returnKey, String reason) {
        ReturnRequest aggregate = requireAggregate(returnKey);
        if (!userContext.userId().equals(aggregate.userId())) {
            throw new ReturnNotFoundException();
        }
        aggregate.openDispute(reason);
        returnRepositoryPort.save(aggregate);
        return currentView(returnKey);
    }

    @Transactional
    public ReturnResult resolveDispute(
            String returnKey, boolean accepted, List<InspectLineCommand> lines, String reason) {
        Map<Long, InspectionDecision> decisions =
                lines == null
                        ? Map.of()
                        : lines.stream()
                                .collect(
                                        Collectors.toMap(
                                                InspectLineCommand::orderLineId,
                                                line ->
                                                        new InspectionDecision(
                                                                line.acceptedQuantity(),
                                                                InventoryDisposition.valueOf(
                                                                        line.disposition()))));
        ReturnRequest aggregate = requireAggregate(returnKey);
        aggregate.resolveDispute(accepted, decisions, reason);
        returnRepositoryPort.save(aggregate);
        return currentView(returnKey);
    }

    /**
     * Issues an idempotent Payment refund. Provider I/O runs outside a local DB transaction. The
     * row-lock based port only prepares/persists ambiguous outcomes.
     */
    public ReturnResult refund(String returnKey) {
        ReturnWorkflowPersistencePort.RefundCandidate candidate =
                workflowPersistencePort.prepareRefund(returnKey);
        RefundPaymentPort.RefundResult result =
                refundPaymentPort.refund(
                        REFUND_KEY_PREFIX + returnKey,
                        candidate.orderId(),
                        returnKey,
                        candidate.refundableAmount());
        switch (result.status()) {
            case UNKNOWN ->
                    workflowPersistencePort.markRefundOutcome(
                            returnKey, ReturnStatus.REFUND_UNKNOWN);
            case FAILED ->
                    workflowPersistencePort.markRefundOutcome(
                            returnKey, ReturnStatus.REFUND_FAILED);
            case SUCCEEDED -> {
                // Keep REFUND_PENDING until the durable PAYMENT_REFUNDED event arrives.
            }
        }
        return currentView(returnKey);
    }

    private ReturnRequest requireAggregate(String returnKey) {
        return returnRepositoryPort.loadByKey(returnKey).orElseThrow(ReturnNotFoundException::new);
    }

    private ReturnResult currentView(String returnKey) {
        return toResult(
                returnQueryPort.findByKey(returnKey).orElseThrow(ReturnNotFoundException::new));
    }

    private ReturnLine toReturnLine(OrderSnapshotPort.OrderSnapshot order, OrderLineSnapshot line) {
        if (line == null) {
            throw new ReturnOrderLineNotFoundException();
        }
        int alreadyReturned =
                returnQueryPort.activeReturnedQuantity(order.orderNo(), line.orderLineId());
        return new ReturnLine(
                line.orderLineId(),
                line.sellerId(),
                line.skuId(),
                line.quantity(),
                alreadyReturned,
                line.quantity() - alreadyReturned,
                line.refundableUnitAmount(),
                order.deliveredAt());
    }

    private void validateReturnWindow(OrderSnapshotPort.OrderSnapshot order) {
        if (!"COMPLETED".equals(order.status()) || order.deliveredAt() == null) {
            throw new InvalidReturnException("only delivered orders are returnable");
        }
        long ageDays = ChronoUnit.DAYS.between(order.deliveredAt(), LocalDateTime.now(clock));
        if (ageDays < 0 || ageDays > RETURN_WINDOW_DAYS) {
            throw new InvalidReturnException("return window has expired");
        }
    }

    private static String deterministicReturnKey(String userId, String requestKey) {
        UUID key =
                UUID.nameUUIDFromBytes(
                        (userId + ':' + requestKey).getBytes(StandardCharsets.UTF_8));
        return RETURN_KEY_PREFIX + key;
    }

    private static ReturnResult toResult(ReturnQueryResult result) {
        return new ReturnResult(
                result.returnKey(),
                result.orderId(),
                result.status(),
                result.refundableAmount(),
                result.receivingWarehouseId(),
                result.createdAt());
    }
}
