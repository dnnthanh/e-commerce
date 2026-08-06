package com.dnnthanh.marketplace.be.checkout.api.adapter.out.persistence;

import com.dnnthanh.marketplace.be.checkout.api.application.port.out.CheckoutProcessPort;
import com.dnnthanh.marketplace.be.checkout.api.domain.enumtype.CheckoutStep;
import com.dnnthanh.marketplace.be.checkout.api.domain.model.CheckoutProcess;
import com.dnnthanh.marketplace.be.platform.stereotype.Persistence;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.annotation.Transactional;

/**
 * PostgreSQL process-manager store with a unique idempotency key and durable compensation flags.
 */
@Persistence
@RequiredArgsConstructor
public class JdbcCheckoutProcessPersistenceAdapter implements CheckoutProcessPort {
    private static final String ACTION_SEPARATOR = "\u001F";
    private final JdbcClient jdbc;

    @Override
    public Optional<CheckoutProcess> findByIdempotencyKey(String idempotencyKey) {
        return jdbc.sql(
                        """
            SELECT checkout_id,idempotency_key,step,promotion_reserved,inventory_reserved,
                   payment_initiated,order_id,completed_actions
            FROM checkout_process_state
            WHERE idempotency_key=:idempotencyKey
            """)
                .param("idempotencyKey", idempotencyKey)
                .query(
                        (rs, row) ->
                                CheckoutProcess.rehydrate(
                                        rs.getString("checkout_id"),
                                        rs.getString("idempotency_key"),
                                        CheckoutStep.valueOf(rs.getString("step")),
                                        rs.getBoolean("promotion_reserved"),
                                        rs.getBoolean("inventory_reserved"),
                                        rs.getBoolean("payment_initiated"),
                                        rs.getString("order_id"),
                                        decodeActions(rs.getString("completed_actions"))))
                .optional();
    }

    @Override
    @Transactional
    public CheckoutProcess save(CheckoutProcess process) {
        jdbc.sql(
                        """
            INSERT INTO checkout_process_state(
                checkout_id,idempotency_key,step,promotion_reserved,inventory_reserved,
                payment_initiated,order_id,completed_actions,created_at,updated_at)
            VALUES(:checkoutId,:idempotencyKey,:step,:promotionReserved,:inventoryReserved,
                :paymentInitiated,:orderId,:completedActions,now(),now())
            ON CONFLICT(idempotency_key) DO UPDATE SET
                step=EXCLUDED.step,
                promotion_reserved=EXCLUDED.promotion_reserved,
                inventory_reserved=EXCLUDED.inventory_reserved,
                payment_initiated=EXCLUDED.payment_initiated,
                order_id=EXCLUDED.order_id,
                completed_actions=EXCLUDED.completed_actions,
                updated_at=now()
            """)
                .param("checkoutId", process.checkoutId())
                .param("idempotencyKey", process.idempotencyKey())
                .param("step", process.step().name())
                .param("promotionReserved", process.promotionReservedFlag())
                .param("inventoryReserved", process.inventoryReservedFlag())
                .param("paymentInitiated", process.paymentInitiatedFlag())
                .param("orderId", process.orderId())
                .param(
                        "completedActions",
                        String.join(ACTION_SEPARATOR, process.completedActions()))
                .update();
        return findByIdempotencyKey(process.idempotencyKey())
                .orElseThrow(
                        () ->
                                new CheckoutPersistenceException(
                                        "Checkout process cannot be reloaded"));
    }

    private List<String> decodeActions(String encoded) {
        if (encoded == null || encoded.isEmpty()) {
            return List.of();
        }
        return Arrays.asList(encoded.split(ACTION_SEPARATOR, -1));
    }

    /** Named infrastructure failure for checkout process persistence. */
    public static final class CheckoutPersistenceException extends RuntimeException {
        public CheckoutPersistenceException(String message) {
            super(message);
        }
    }
}
