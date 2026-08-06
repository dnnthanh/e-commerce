package com.dnnthanh.marketplace.be.checkout.api.application;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dnnthanh.marketplace.be.checkout.api.application.model.CheckoutCommand;
import com.dnnthanh.marketplace.be.checkout.api.application.model.CheckoutItemCommand;
import com.dnnthanh.marketplace.be.checkout.api.application.model.PaymentProvider;
import com.dnnthanh.marketplace.be.checkout.api.application.model.PromotionReservation;
import com.dnnthanh.marketplace.be.checkout.api.application.port.out.CheckoutSagaRepositoryPort;
import com.dnnthanh.marketplace.be.checkout.api.application.port.out.InventoryClientPort;
import com.dnnthanh.marketplace.be.checkout.api.application.port.out.OrderClientPort;
import com.dnnthanh.marketplace.be.checkout.api.application.port.out.PaymentClientPort;
import com.dnnthanh.marketplace.be.checkout.api.application.port.out.PricingClientPort;
import com.dnnthanh.marketplace.be.checkout.api.application.port.out.PromotionClientPort;
import com.dnnthanh.marketplace.be.checkout.api.application.service.CheckoutServiceImplement;
import com.dnnthanh.marketplace.be.checkout.api.domain.model.CheckoutSaga;
import com.dnnthanh.marketplace.be.platform.context.UserContext;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

/** Failure-path tests for the Checkout Saga orchestration boundary. */
@ExtendWith(MockitoExtension.class)
class CheckoutOrchestratorTest {

    @Mock private PricingClientPort pricing;
    @Mock private PromotionClientPort promotion;
    @Mock private InventoryClientPort inventory;
    @Mock private OrderClientPort order;
    @Mock private PaymentClientPort payment;
    @Mock private CheckoutSagaRepositoryPort sagas;

    private CheckoutServiceImplement orchestrator;

    @BeforeEach
    void setUp() {
        UserContext user =
                new UserContext("user-1", "buyer", UserContext.ActorType.USER, Set.of("CUSTOMER"));
        orchestrator =
                new CheckoutServiceImplement(
                        pricing,
                        promotion,
                        inventory,
                        order,
                        payment,
                        sagas,
                        user,
                        new ObjectMapper());
        when(sagas.find("checkout-1")).thenReturn(Optional.empty());
        when(sagas.save(any(CheckoutSaga.class), anyString()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(pricing.price(1001L)).thenReturn(new BigDecimal("100.00"));
        when(promotion.reserve(
                        "checkout-1", "user-1", new BigDecimal("100.00"), List.of("PROMO10")))
                .thenReturn(
                        new PromotionReservation(new BigDecimal("10.00"), List.of("promotion-1")));
    }

    @Test
    void releasesPromotionWhenInventoryReservationFails() {
        when(inventory.reserve(anyString(), any()))
                .thenThrow(new IllegalStateException("inventory down"));

        assertThrows(IllegalStateException.class, () -> orchestrator.checkout(command()));

        verify(promotion).release("checkout-1", List.of("promotion-1"));
        verify(order, never()).create(anyString(), anyString(), any(), any(), any());
    }

    @Test
    void doesNotReleaseReservationsAfterOrderExists() {
        when(inventory.reserve("checkout-1", command().items()))
                .thenReturn(List.of("checkout-1-0"));
        when(order.create(anyString(), anyString(), any(), any(), any())).thenReturn("ORD-1");
        doThrow(new IllegalStateException("inventory attach timeout"))
                .when(inventory)
                .attachOrder(List.of("checkout-1-0"), "ORD-1");

        assertThrows(IllegalStateException.class, () -> orchestrator.checkout(command()));

        verify(inventory, never()).release(List.of("checkout-1-0"));
        verify(promotion, never()).release("checkout-1", List.of("promotion-1"));
    }

    private static CheckoutCommand command() {
        return new CheckoutCommand(
                "checkout-1",
                List.of(new CheckoutItemCommand(10L, 1001L, 20L, 1)),
                List.of("PROMO10"),
                PaymentProvider.VNPAY);
    }
}
