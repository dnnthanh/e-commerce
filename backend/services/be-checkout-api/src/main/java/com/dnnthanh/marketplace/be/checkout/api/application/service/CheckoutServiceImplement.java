package com.dnnthanh.marketplace.be.checkout.api.application.service;

import com.dnnthanh.marketplace.be.checkout.api.application.exception.CheckoutSnapshotException;
import com.dnnthanh.marketplace.be.checkout.api.application.model.CheckoutCommand;
import com.dnnthanh.marketplace.be.checkout.api.application.model.CheckoutItemCommand;
import com.dnnthanh.marketplace.be.checkout.api.application.model.CheckoutResult;
import com.dnnthanh.marketplace.be.checkout.api.application.model.PaymentResult;
import com.dnnthanh.marketplace.be.checkout.api.application.model.PricedOrderLine;
import com.dnnthanh.marketplace.be.checkout.api.application.model.PromotionReservation;
import com.dnnthanh.marketplace.be.checkout.api.application.port.in.CheckoutUseCase;
import com.dnnthanh.marketplace.be.checkout.api.application.port.out.CheckoutSagaRepositoryPort;
import com.dnnthanh.marketplace.be.checkout.api.application.port.out.InventoryClientPort;
import com.dnnthanh.marketplace.be.checkout.api.application.port.out.OrderClientPort;
import com.dnnthanh.marketplace.be.checkout.api.application.port.out.PaymentClientPort;
import com.dnnthanh.marketplace.be.checkout.api.application.port.out.PricingClientPort;
import com.dnnthanh.marketplace.be.checkout.api.application.port.out.PromotionClientPort;
import com.dnnthanh.marketplace.be.checkout.api.domain.enumtype.CheckoutPaymentStatus;
import com.dnnthanh.marketplace.be.checkout.api.domain.model.CheckoutSaga;
import com.dnnthanh.marketplace.be.platform.context.UserContext;
import com.dnnthanh.marketplace.be.platform.stereotype.UseCase;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

/**
 * Persistent Saga/process manager. Remote calls occur outside local DB transactions and every side
 * effect uses a stable business idempotency key.
 */
@UseCase
@RequiredArgsConstructor
public class CheckoutServiceImplement implements CheckoutUseCase {
    private final PricingClientPort pricing;
    private final PromotionClientPort promotion;
    private final InventoryClientPort inventory;
    private final OrderClientPort order;
    private final PaymentClientPort payment;
    private final CheckoutSagaRepositoryPort sagas;
    private final UserContext user;
    private final ObjectMapper json;

    @Override
    public CheckoutResult checkout(CheckoutCommand command) {
        CheckoutSaga existing = sagas.find(command.checkoutKey()).orElse(null);
        if (existing != null && existing.state() == CheckoutSaga.State.COMPLETED) {
            return response(existing, CheckoutPaymentStatus.PAID, null);
        }

        CheckoutSaga saga =
                existing == null
                        ? new CheckoutSaga(command.checkoutKey(), user.userId())
                        : existing;
        String snapshot = serialize(command);
        List<String> inventoryReservationKeys = new ArrayList<>();
        PromotionReservation promotionReservation =
                new PromotionReservation(BigDecimal.ZERO, List.of());

        try {
            List<PricedOrderLine> pricedLines = new ArrayList<>();
            BigDecimal gross = BigDecimal.ZERO;
            for (CheckoutItemCommand item : command.items()) {
                BigDecimal amount = pricing.price(item.skuId());
                gross = gross.add(amount.multiply(BigDecimal.valueOf(item.quantity())));
                pricedLines.add(
                        new PricedOrderLine(
                                item.sellerId(), item.skuId(), item.quantity(), amount));
            }

            promotionReservation =
                    promotion.reserve(
                            command.checkoutKey(), saga.userId(), gross, command.promotionCodes());
            inventoryReservationKeys = inventory.reserve(command.checkoutKey(), command.items());
            saga.reserved();
            sagas.save(saga, snapshot);

            String orderNo =
                    order.create(
                            command.checkoutKey(),
                            saga.userId(),
                            pricedLines,
                            gross,
                            promotionReservation.totalDiscount());
            // Persist the exactly-once Order fact before subsequent remote side effects. A retry
            // can
            // safely call the Order idempotency boundary again using the same checkout key.
            saga.ordered(orderNo);
            sagas.save(saga, snapshot);

            inventory.attachOrder(inventoryReservationKeys, orderNo);
            promotion.confirm(command.checkoutKey(), promotionReservation.promotionIds());

            String paymentKey = "PAY-" + command.checkoutKey();
            PaymentResult result =
                    payment.create(
                            paymentKey,
                            orderNo,
                            saga.userId(),
                            command.provider(),
                            gross.subtract(promotionReservation.totalDiscount()));
            saga.payment(paymentKey, result.status());
            sagas.save(saga, snapshot);
            return response(saga, result.status(), result.redirectUrl());
        } catch (RuntimeException failure) {
            // Before Order creation both reservation systems are compensatable and duplicate-safe.
            // After Order creation they must remain attached to the Order and recovery continues
            // forward instead of silently releasing committed business capacity.
            if (saga.orderNo() == null) {
                inventory.release(inventoryReservationKeys);
                promotion.release(command.checkoutKey(), promotionReservation.promotionIds());
            }
            saga.retryLater();
            sagas.save(saga, snapshot);
            throw failure;
        }
    }

    private CheckoutResult response(
            CheckoutSaga saga, CheckoutPaymentStatus paymentStatus, String redirectUrl) {
        return new CheckoutResult(
                saga.checkoutKey(), saga.state(), saga.orderNo(), paymentStatus, redirectUrl);
    }

    private String serialize(CheckoutCommand command) {
        try {
            return json.writeValueAsString(command);
        } catch (Exception failure) {
            throw new CheckoutSnapshotException("Checkout request cannot be serialized", failure);
        }
    }
}
