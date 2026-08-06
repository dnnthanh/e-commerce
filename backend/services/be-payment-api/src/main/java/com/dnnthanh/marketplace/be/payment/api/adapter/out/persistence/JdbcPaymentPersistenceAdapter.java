package com.dnnthanh.marketplace.be.payment.api.adapter.out.persistence;

import com.dnnthanh.marketplace.be.payment.api.adapter.out.persistence.exception.PaymentPersistenceException;
import com.dnnthanh.marketplace.be.payment.api.application.port.out.PaymentRepositoryPort;
import com.dnnthanh.marketplace.be.payment.api.application.port.out.PaymentWorkflowPersistencePort;
import com.dnnthanh.marketplace.be.payment.api.application.port.out.RefundPersistencePort;
import com.dnnthanh.marketplace.be.payment.api.domain.enumtype.PaymentEventType;
import com.dnnthanh.marketplace.be.payment.api.domain.enumtype.RefundStatus;
import com.dnnthanh.marketplace.be.payment.api.domain.exception.InvalidRefundAmountException;
import com.dnnthanh.marketplace.be.payment.api.domain.model.Payment;
import com.dnnthanh.marketplace.be.platform.stereotype.Persistence;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

/**
 * PostgreSQL payment adapter. JDBC is intentional here for atomic ledger/outbox and row-lock
 * operations; application code only depends on ports.
 */
@Persistence
@RequiredArgsConstructor
public class JdbcPaymentPersistenceAdapter
        implements PaymentRepositoryPort, PaymentWorkflowPersistencePort, RefundPersistencePort {
    private final JdbcClient jdbc;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public Payment save(Payment payment) {
        if (payment.id() == null) {
            Long id =
                    jdbc.sql(
                                    """
              INSERT INTO payment(payment_key,order_id,user_id,provider,amount,currency,status,
                  provider_transaction_id,version,created_at,updated_at)
              VALUES(:key,:orderId,:userId,:provider,:amount,:currency,:status,:transactionId,0,
                  :createdAt,:updatedAt)
              RETURNING id
              """)
                            .param("key", payment.paymentKey())
                            .param("orderId", payment.orderId())
                            .param("userId", payment.userId())
                            .param("provider", payment.provider().name())
                            .param("amount", payment.amount())
                            .param("currency", payment.currency())
                            .param("status", payment.status().name())
                            .param("transactionId", payment.providerTransactionId())
                            .param("createdAt", payment.createdAt())
                            .param("updatedAt", payment.updatedAt())
                            .query(Long.class)
                            .single();
            return findById(id)
                    .orElseThrow(
                            () ->
                                    new PaymentPersistenceException(
                                            "Inserted payment could not be reloaded: " + id, null));
        }

        int changed =
                jdbc.sql(
                                """
            UPDATE payment
            SET status=:status,provider_transaction_id=:transactionId,version=version+1,updated_at=:updatedAt
            WHERE id=:id
            """)
                        .param("status", payment.status().name())
                        .param("transactionId", payment.providerTransactionId())
                        .param("updatedAt", payment.updatedAt())
                        .param("id", payment.id())
                        .update();
        if (changed != 1) {
            throw new PaymentPersistenceException("Payment update lost: " + payment.id(), null);
        }
        return findById(payment.id())
                .orElseThrow(
                        () ->
                                new PaymentPersistenceException(
                                        "Updated payment could not be reloaded: " + payment.id(),
                                        null));
    }

    @Override
    public Optional<Payment> findByKey(String key) {
        return jdbc.sql(PAYMENT_SELECT + " WHERE p.payment_key=:key")
                .param("key", key)
                .query(this::mapPayment)
                .optional();
    }

    @Override
    public Optional<Payment> findByOrder(String orderId) {
        return jdbc.sql(PAYMENT_SELECT + " WHERE p.order_id=:orderId ORDER BY p.id DESC LIMIT 1")
                .param("orderId", orderId)
                .query(this::mapPayment)
                .optional();
    }

    @Override
    public Optional<Payment> findById(Long id) {
        return jdbc.sql(PAYMENT_SELECT + " WHERE p.id=:id")
                .param("id", id)
                .query(this::mapPayment)
                .optional();
    }

    @Override
    @Transactional
    public Payment saveWithEvent(Payment payment, PaymentEventType eventType) {
        Payment saved = save(payment);
        insertOutbox(
                saved.paymentKey(),
                eventType.name(),
                Map.of(
                        "paymentKey", saved.paymentKey(),
                        "orderId", saved.orderId(),
                        "userId", saved.userId(),
                        "provider", saved.provider().name(),
                        "status", saved.status().name(),
                        "amount", saved.amount()));
        return saved;
    }

    @Override
    public boolean claimProviderEvent(String eventId) {
        try {
            jdbc.sql(
                            """
              INSERT INTO inbox_event(consumer_name,event_id,processed_at)
              VALUES('PAYMENT_PROVIDER_CALLBACK',:eventId,:processedAt)
              """)
                    .param("eventId", eventId)
                    .param("processedAt", LocalDateTime.now())
                    .update();
            return true;
        } catch (DataIntegrityViolationException duplicate) {
            return false;
        }
    }

    @Override
    @Transactional
    public void scheduleReconciliation(Payment payment, String error) {
        LocalDateTime now = LocalDateTime.now();
        jdbc.sql(
                        """
            INSERT INTO payment_reconciliation_state(payment_id,retry_count,next_retry_at,last_error,updated_at)
            VALUES(:paymentId,0,:nextRetry,:error,:updatedAt)
            ON CONFLICT(payment_id) DO UPDATE SET
              retry_count=payment_reconciliation_state.retry_count+1,
              next_retry_at=EXCLUDED.next_retry_at,last_error=EXCLUDED.last_error,
              updated_at=EXCLUDED.updated_at
            """)
                .param("paymentId", payment.id())
                .param("nextRetry", now.plusMinutes(2))
                .param("error", error)
                .param("updatedAt", now)
                .update();
    }

    @Override
    @Transactional
    public void clearReconciliation(Payment payment) {
        jdbc.sql("DELETE FROM payment_reconciliation_state WHERE payment_id=:paymentId")
                .param("paymentId", payment.id())
                .update();
    }

    @Override
    public Optional<RefundSnapshot> findRefundByKey(String refundKey) {
        return jdbc.sql(
                        """
            SELECT r.refund_key,r.status,r.amount,p.status AS payment_status
            FROM refund r JOIN payment p ON p.id=r.payment_id
            WHERE r.refund_key=:refundKey
            """)
                .param("refundKey", refundKey)
                .query(
                        (rs, row) ->
                                new RefundSnapshot(
                                        rs.getString(1),
                                        RefundStatus.valueOf(rs.getString(2)),
                                        rs.getBigDecimal(3),
                                        Payment.Status.valueOf(rs.getString(4))))
                .optional();
    }

    @Override
    @Transactional
    public RefundSnapshot persistOutcome(
            Payment payment,
            String refundKey,
            String returnKey,
            BigDecimal amount,
            RefundStatus status,
            String providerRefundId) {
        Optional<RefundSnapshot> duplicate = findRefundByKey(refundKey);
        if (duplicate.isPresent()) return duplicate.get();

        // Serialize refunds per payment so concurrent partial refunds cannot exceed captured
        // amount.
        jdbc.sql("SELECT id FROM payment WHERE id=:paymentId FOR UPDATE")
                .param("paymentId", payment.id())
                .query(Long.class)
                .single();
        BigDecimal succeeded =
                jdbc.sql(
                                """
            SELECT COALESCE(SUM(amount),0) FROM refund
            WHERE payment_id=:paymentId AND status='SUCCEEDED'
            """)
                        .param("paymentId", payment.id())
                        .query(BigDecimal.class)
                        .single();
        if (status == RefundStatus.SUCCEEDED
                && succeeded.add(amount).compareTo(payment.amount()) > 0) {
            throw new InvalidRefundAmountException("Refund exceeds captured amount");
        }

        LocalDateTime now = LocalDateTime.now();
        jdbc.sql(
                        """
            INSERT INTO refund(refund_key,payment_id,amount,status,provider_refund_id,created_at,updated_at)
            VALUES(:refundKey,:paymentId,:amount,:status,:providerRefundId,:createdAt,:updatedAt)
            """)
                .param("refundKey", refundKey)
                .param("paymentId", payment.id())
                .param("amount", amount)
                .param("status", status.name())
                .param("providerRefundId", providerRefundId)
                .param("createdAt", now)
                .param("updatedAt", now)
                .update();

        if (status == RefundStatus.SUCCEEDED) {
            payment.applySuccessfulRefund(amount);
            save(payment);
            insertOutbox(
                    payment.paymentKey(),
                    PaymentEventType.PAYMENT_REFUNDED.name(),
                    Map.of(
                            "refundKey",
                            refundKey,
                            "returnKey",
                            returnKey == null ? "" : returnKey,
                            "orderId",
                            payment.orderId(),
                            "paymentKey",
                            payment.paymentKey(),
                            "amount",
                            amount,
                            "paymentStatus",
                            payment.status().name()));
        }
        return new RefundSnapshot(refundKey, status, amount, payment.status());
    }

    private void insertOutbox(String aggregateId, String eventType, Map<String, Object> payload) {
        try {
            jdbc.sql(
                            """
              INSERT INTO outbox_event(event_id,aggregate_id,event_type,payload_json,status,created_at)
              VALUES(:eventId,:aggregateId,:eventType,CAST(:payload AS jsonb),'PENDING',:createdAt)
              """)
                    .param("eventId", UUID.randomUUID().toString())
                    .param("aggregateId", aggregateId)
                    .param("eventType", eventType)
                    .param("payload", objectMapper.writeValueAsString(payload))
                    .param("createdAt", LocalDateTime.now())
                    .update();
        } catch (Exception failure) {
            throw new PaymentPersistenceException(
                    "Payment outbox payload cannot be serialized", failure);
        }
    }

    private static final String PAYMENT_SELECT =
            """
      SELECT p.*, COALESCE((
        SELECT SUM(r.amount) FROM refund r
        WHERE r.payment_id=p.id AND r.status='SUCCEEDED'
      ),0) AS refunded_amount
      FROM payment p
      """;

    private Payment mapPayment(ResultSet rs, int row) throws SQLException {
        return Payment.rehydrate(
                rs.getLong("id"),
                rs.getString("payment_key"),
                rs.getString("order_id"),
                rs.getString("user_id"),
                Payment.Provider.valueOf(rs.getString("provider")),
                rs.getBigDecimal("amount"),
                rs.getString("currency"),
                Payment.Status.valueOf(rs.getString("status")),
                rs.getString("provider_transaction_id"),
                rs.getBigDecimal("refunded_amount"),
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getTimestamp("updated_at").toLocalDateTime());
    }
}
