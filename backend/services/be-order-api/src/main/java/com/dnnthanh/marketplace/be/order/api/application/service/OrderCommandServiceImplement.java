package com.dnnthanh.marketplace.be.order.api.application.service;

import com.dnnthanh.marketplace.be.order.api.application.command.CancelOrderCommand;
import com.dnnthanh.marketplace.be.order.api.application.command.CreateOrderCommand;
import com.dnnthanh.marketplace.be.order.api.application.command.OrderEventCommand;
import com.dnnthanh.marketplace.be.order.api.application.exception.InvalidOrderCommandException;
import com.dnnthanh.marketplace.be.order.api.application.exception.OrderNotFoundException;
import com.dnnthanh.marketplace.be.order.api.application.exception.OrderStateConflictException;
import com.dnnthanh.marketplace.be.order.api.application.port.in.OrderCommandUseCase;
import com.dnnthanh.marketplace.be.order.api.application.port.out.OrderRepositoryPort;
import com.dnnthanh.marketplace.be.order.api.domain.constant.OrderConstants;
import com.dnnthanh.marketplace.be.order.api.domain.enumtype.OrderEventType;
import com.dnnthanh.marketplace.be.order.api.domain.exception.InvalidOrderStateException;
import com.dnnthanh.marketplace.be.order.api.domain.exception.InvalidOrderValidationException;
import com.dnnthanh.marketplace.be.order.api.domain.model.MarketplaceOrder;
import com.dnnthanh.marketplace.be.platform.context.UserContext;
import com.dnnthanh.marketplace.be.platform.stereotype.UseCase;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

/** Transactional Order command use cases with explicit idempotency and lifecycle boundaries. */
@UseCase
@RequiredArgsConstructor
public class OrderCommandServiceImplement implements OrderCommandUseCase {

    private final OrderRepositoryPort orderRepository;

    private final Clock clock;
    private final UserContext userContext;

    @Transactional
    public MarketplaceOrder create(CreateOrderCommand command) {
        return orderRepository
                .findByCheckoutKey(command.checkoutKey())
                .orElseGet(
                        () -> {
                            try {
                                MarketplaceOrder order =
                                        MarketplaceOrder.create(
                                                OrderConstants.ORDER_NUMBER_PREFIX
                                                        + command.checkoutKey(),
                                                command.checkoutKey(),
                                                command.userId(),
                                                command.lines().stream()
                                                        .map(
                                                                line ->
                                                                        new MarketplaceOrder
                                                                                .LineCommand(
                                                                                line.sellerId(),
                                                                                line.skuId(),
                                                                                line.quantity(),
                                                                                line.unitPrice()))
                                                        .toList(),
                                                command.grossAmount(),
                                                command.discountAmount(),
                                                LocalDateTime.now(clock));
                                return orderRepository.save(order, OrderEventType.ORDER_CREATED);
                            } catch (InvalidOrderValidationException exception) {
                                throw new InvalidOrderCommandException();
                            }
                        });
    }

    /** Cancels an owned unpaid order. */
    @Transactional
    public MarketplaceOrder cancel(CancelOrderCommand command) {
        MarketplaceOrder order = requireOwned(command.orderNo(), userContext.userId());
        try {
            order.cancel(command.reason());
        } catch (InvalidOrderStateException exception) {
            throw new OrderStateConflictException();
        }
        return orderRepository.save(order, OrderEventType.ORDER_CANCELLED);
    }

    /** Idempotently applies a successful payment event. */
    @Transactional
    public MarketplaceOrder markPaid(OrderEventCommand command) {
        MarketplaceOrder order = require(command.orderNo());
        if (!orderRepository.claimInboxEvent(
                OrderConstants.PAYMENT_INBOX_CONSUMER, command.eventId())) {
            return order;
        }
        try {
            order.markPaid();
        } catch (InvalidOrderStateException exception) {
            throw new OrderStateConflictException();
        }
        return orderRepository.save(order, OrderEventType.ORDER_PAID);
    }

    /** Idempotently starts fulfillment. */
    @Transactional
    public MarketplaceOrder markFulfilling(OrderEventCommand command) {
        MarketplaceOrder order = require(command.orderNo());
        if (!orderRepository.claimInboxEvent(
                OrderConstants.FULFILLMENT_INBOX_CONSUMER, command.eventId())) {
            return order;
        }
        try {
            order.markFulfilling();
        } catch (InvalidOrderStateException exception) {
            throw new OrderStateConflictException();
        }
        return orderRepository.save(order, OrderEventType.ORDER_FULFILLING);
    }

    /** Idempotently completes fulfillment. */
    @Transactional
    public MarketplaceOrder markCompleted(OrderEventCommand command) {
        MarketplaceOrder order = require(command.orderNo());
        String consumer = OrderConstants.FULFILLMENT_INBOX_CONSUMER + "-completed";
        if (!orderRepository.claimInboxEvent(consumer, command.eventId())) {
            return order;
        }
        try {
            order.markCompleted();
        } catch (InvalidOrderStateException exception) {
            throw new OrderStateConflictException();
        }
        return orderRepository.save(order, OrderEventType.ORDER_COMPLETED);
    }

    /** Expires a bounded batch of unpaid orders. Suitable for scheduler-driven recovery jobs. */
    @Transactional
    public List<MarketplaceOrder> expireUnpaidBatch() {
        LocalDateTime cutoff =
                LocalDateTime.now(clock).minusMinutes(OrderConstants.UNPAID_EXPIRY_MINUTES);
        return orderRepository.findUnpaidBefore(cutoff, OrderConstants.EXPIRY_BATCH_SIZE).stream()
                .map(
                        order -> {
                            try {
                                order.expireUnpaid(cutoff);
                                return orderRepository.save(order, OrderEventType.ORDER_EXPIRED);
                            } catch (InvalidOrderStateException
                                    | InvalidOrderValidationException race) {
                                return order;
                            }
                        })
                .toList();
    }

    /** Seller-scoped cancellation before physical fulfillment begins. */
    @Transactional
    public MarketplaceOrder cancelSellerOrder(
            String orderNo,
            Long sellerId,
            com.dnnthanh.marketplace.be.order.api.domain.enumtype.CancellationReason reason) {
        MarketplaceOrder order = require(orderNo);
        try {
            order.cancelSellerOrder(sellerId, reason);
        } catch (InvalidOrderStateException | InvalidOrderValidationException exception) {
            throw new OrderStateConflictException();
        }
        return orderRepository.save(order, OrderEventType.SELLER_ORDER_CANCELLED);
    }

    /** Guarded operations-only recovery command with explicit downstream compensation proof. */
    @Transactional
    public MarketplaceOrder manualOverrideCancel(
            String orderNo,
            com.dnnthanh.marketplace.be.order.api.domain.enumtype.CancellationReason reason,
            boolean downstreamCompensationConfirmed) {
        MarketplaceOrder order = require(orderNo);
        try {
            order.manualOverrideCancel(reason, downstreamCompensationConfirmed);
        } catch (InvalidOrderStateException | InvalidOrderValidationException exception) {
            throw new OrderStateConflictException();
        }
        return orderRepository.save(order, OrderEventType.ORDER_MANUAL_OVERRIDE);
    }

    private MarketplaceOrder requireOwned(String orderNo, String requesterUserId) {
        MarketplaceOrder order = require(orderNo);
        if (!Objects.equals(order.userId(), requesterUserId)) {
            throw new OrderNotFoundException();
        }
        return order;
    }

    private MarketplaceOrder require(String orderNo) {
        return orderRepository.findByOrderNo(orderNo).orElseThrow(OrderNotFoundException::new);
    }
}
