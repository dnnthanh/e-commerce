package com.dnnthanh.marketplace.be.returns.api.adapter.out.persistence;

import com.dnnthanh.marketplace.be.platform.outbox.OutboxPayloadCodec;
import com.dnnthanh.marketplace.be.platform.stereotype.Persistence;
import com.dnnthanh.marketplace.be.returns.api.application.exception.ReturnNotFoundException;
import com.dnnthanh.marketplace.be.returns.api.application.port.out.ReturnWorkflowPersistencePort;
import com.dnnthanh.marketplace.be.returns.api.domain.enumtype.ReturnEventType;
import com.dnnthanh.marketplace.be.returns.api.domain.enumtype.ReturnStatus;
import com.dnnthanh.marketplace.be.returns.api.domain.exception.ReturnStateConflictException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.annotation.Transactional;

/** PostgreSQL row-lock adapter reserved for ambiguous refund outcome coordination. */
@Persistence
@RequiredArgsConstructor
public class JdbcReturnWorkflowPersistenceAdapter implements ReturnWorkflowPersistencePort {
    private static final String OUTBOX_PENDING = "PENDING";

    private final JdbcClient jdbc;
    private final OutboxPayloadCodec outboxPayloadCodec;

    @Override
    @Transactional
    public RefundCandidate prepareRefund(String returnKey) {
        Map<String, Object> row =
                jdbc.sql(
                                """
                        SELECT order_id, refundable_amount, status
                        FROM return_request
                        WHERE return_key=:returnKey
                        FOR UPDATE
                        """)
                        .param("returnKey", returnKey)
                        .query(new ColumnMapRowMapper())
                        .optional()
                        .orElseThrow(ReturnNotFoundException::new);

        ReturnStatus current = ReturnStatus.valueOf(String.valueOf(row.get("status")));
        if (!current.canPrepareRefund()) {
            throw new ReturnStateConflictException(
                    "Return %s cannot prepare refund from %s".formatted(returnKey, current));
        }
        if (current != ReturnStatus.REFUND_PENDING) {
            jdbc.sql(
                            """
                            UPDATE return_request
                            SET status=:pending, updated_at=:updatedAt, version=version+1
                            WHERE return_key=:returnKey AND status=:current
                            """)
                    .param("pending", ReturnStatus.REFUND_PENDING.name())
                    .param("updatedAt", LocalDateTime.now())
                    .param("returnKey", returnKey)
                    .param("current", current.name())
                    .update();
            emit(
                    returnKey,
                    ReturnEventType.RETURN_REFUND_REQUESTED,
                    Map.of("returnKey", returnKey, "orderId", String.valueOf(row.get("order_id"))));
        }
        return new RefundCandidate(
                String.valueOf(row.get("order_id")), (BigDecimal) row.get("refundable_amount"));
    }

    @Override
    @Transactional
    public void markRefundOutcome(String returnKey, ReturnStatus status) {
        if (status != ReturnStatus.REFUND_UNKNOWN && status != ReturnStatus.REFUND_FAILED) {
            throw new ReturnStateConflictException(
                    "Only unknown/failed refund outcomes may be persisted here");
        }
        int changed =
                jdbc.sql(
                                """
                        UPDATE return_request
                        SET status=:status, updated_at=:updatedAt, version=version+1
                        WHERE return_key=:returnKey AND status=:pending
                        """)
                        .param("status", status.name())
                        .param("updatedAt", LocalDateTime.now())
                        .param("returnKey", returnKey)
                        .param("pending", ReturnStatus.REFUND_PENDING.name())
                        .update();
        if (changed == 1) {
            emit(
                    returnKey,
                    status == ReturnStatus.REFUND_UNKNOWN
                            ? ReturnEventType.RETURN_REFUND_UNKNOWN
                            : ReturnEventType.RETURN_REFUND_FAILED,
                    Map.of("returnKey", returnKey, "status", status.name()));
            return;
        }
        String current =
                jdbc.sql("SELECT status FROM return_request WHERE return_key=:returnKey")
                        .param("returnKey", returnKey)
                        .query(String.class)
                        .optional()
                        .orElseThrow(ReturnNotFoundException::new);
        if (!ReturnStatus.COMPLETED.name().equals(current)) {
            throw new ReturnStateConflictException(
                    "Return %s changed state while refund result was persisted"
                            .formatted(returnKey));
        }
    }

    private void emit(String returnKey, ReturnEventType eventType, Map<String, Object> payload) {
        jdbc.sql(
                        """
                        INSERT INTO outbox_event(
                            event_id, aggregate_id, event_type, payload_json, status, created_at)
                        VALUES(:eventId, :aggregateId, :eventType, CAST(:payload AS jsonb), :status, :createdAt)
                        """)
                .param("eventId", UUID.randomUUID().toString())
                .param("aggregateId", returnKey)
                .param("eventType", eventType.name())
                .param("payload", outboxPayloadCodec.write(payload))
                .param("status", OUTBOX_PENDING)
                .param("createdAt", LocalDateTime.now())
                .update();
    }
}
