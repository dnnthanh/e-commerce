package com.dnnthanh.marketplace.be.order.api.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.dnnthanh.marketplace.be.order.api.domain.enumtype.CancellationReason;
import com.dnnthanh.marketplace.be.order.api.domain.enumtype.OrderStatus;
import com.dnnthanh.marketplace.be.order.api.domain.exception.InvalidOrderStateException;
import com.dnnthanh.marketplace.be.order.api.domain.exception.InvalidOrderValidationException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Business-level tests for order creation, money allocation and lifecycle transitions. */
class MarketplaceOrderTest {

    @Test
    void allocatesDiscountExactlyAcrossSellerLines() {
        MarketplaceOrder order = newOrder();

        BigDecimal lineDiscount =
                order.sellerOrders().stream()
                        .flatMap(seller -> seller.lines().stream())
                        .map(MarketplaceOrder.OrderLine::allocatedDiscount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal lineNet =
                order.sellerOrders().stream()
                        .flatMap(seller -> seller.lines().stream())
                        .map(MarketplaceOrder.OrderLine::netAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        assertEquals(new BigDecimal("50.00"), lineDiscount);
        assertEquals(new BigDecimal("250.00"), lineNet);
        assertEquals(new BigDecimal("250.00"), order.payableAmount());
    }

    @Test
    void rejectsGrossThatDoesNotMatchLines() {
        assertThrows(
                InvalidOrderValidationException.class,
                () ->
                        MarketplaceOrder.create(
                                "ORD-1",
                                "checkout-1",
                                "U1",
                                List.of(
                                        new MarketplaceOrder.LineCommand(
                                                1L, 1L, 1, BigDecimal.TEN)),
                                new BigDecimal("9"),
                                BigDecimal.ZERO,
                                LocalDateTime.now()));
    }

    @Test
    void paidOrderCannotBeCancelledByCustomer() {
        MarketplaceOrder order = newOrder();
        order.markPaymentPending();
        order.markPaid();

        assertThrows(
                InvalidOrderStateException.class,
                () -> order.cancel(CancellationReason.CUSTOMER_REQUEST));
    }

    @Test
    void followsPaymentAndFulfillmentStateMachine() {
        MarketplaceOrder order = newOrder();

        order.markPaymentPending();
        order.markPaid();
        order.markFulfilling();
        order.markCompleted();

        assertEquals(OrderStatus.COMPLETED, order.status());
    }

    private MarketplaceOrder newOrder() {
        return MarketplaceOrder.create(
                "ORD-1",
                "checkout-1",
                "U1",
                List.of(
                        new MarketplaceOrder.LineCommand(1L, 11L, 1, new BigDecimal("100.00")),
                        new MarketplaceOrder.LineCommand(2L, 22L, 1, new BigDecimal("200.00"))),
                new BigDecimal("300.00"),
                new BigDecimal("50.00"),
                LocalDateTime.now());
    }
}
