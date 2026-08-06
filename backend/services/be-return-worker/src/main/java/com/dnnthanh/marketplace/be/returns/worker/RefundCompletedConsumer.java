package com.dnnthanh.marketplace.be.returns.worker;

import com.dnnthanh.marketplace.be.platform.event.DomainEvent;
import com.dnnthanh.marketplace.be.platform.kafka.BaseDomainEventConsumer;
import com.dnnthanh.marketplace.be.platform.outbox.OutboxPayloadCodec;
import com.dnnthanh.marketplace.be.platform.stereotype.Adapter;
import com.dnnthanh.marketplace.be.returns.worker.exception.ReturnRefundProjectionException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.transaction.annotation.Transactional;

/**
 * Finalizes returns only from the durable PAYMENT_REFUNDED event and emits idempotent restock
 * commands.
 */
@Adapter
@RequiredArgsConstructor
public class RefundCompletedConsumer extends BaseDomainEventConsumer {

    private final JdbcClient jdbc;

    private final OutboxPayloadCodec outboxPayloadCodec;

    /**
     * Claims the payment event, completes the return and writes restock Outbox rows in one
     * transaction.
     *
     * @param event typed payment event
     */
    @Transactional
    @KafkaListener(topics = "marketplace.payment.events", groupId = "return-refund-v2")
    public void onPayment(DomainEvent event) {
        if (!accepts(event, "PAYMENT_REFUNDED")) {
            return;
        }
        Object returnKeyValue = event.payload().get("returnKey");
        if (returnKeyValue == null || !claim(event.eventId())) {
            return;
        }
        String returnKey = String.valueOf(returnKeyValue);
        Map<String, Object> request =
                jdbc.sql(
                                """
                SELECT id, status, receiving_warehouse_id
                FROM return_request
                WHERE return_key=:returnKey
                FOR UPDATE
                """)
                        .param("returnKey", returnKey)
                        .query(new ColumnMapRowMapper())
                        .optional()
                        .orElse(null);
        if (request == null || "COMPLETED".equals(request.get("status"))) {
            return;
        }
        Long warehouseId =
                request.get("receiving_warehouse_id") == null
                        ? null
                        : ((Number) request.get("receiving_warehouse_id")).longValue();
        if (warehouseId == null) {
            throw new ReturnRefundProjectionException(
                    "Return " + returnKey + " has no receiving warehouse");
        }
        long returnId = ((Number) request.get("id")).longValue();
        jdbc.sql(
                        """
                UPDATE return_request
                SET status='COMPLETED', updated_at=:updatedAt
                WHERE id=:returnId
                """)
                .param("updatedAt", LocalDateTime.now())
                .param("returnId", returnId)
                .update();

        for (Map<String, Object> line :
                jdbc.sql(
                                """
                SELECT sku_id, quantity FROM return_line WHERE return_id=:returnId
                """)
                        .param("returnId", returnId)
                        .query()
                        .listOfRows()) {
            Map<String, Object> payload =
                    Map.of(
                            "returnKey",
                            returnKey,
                            "skuId",
                            line.get("sku_id"),
                            "warehouseId",
                            warehouseId,
                            "quantity",
                            line.get("quantity"));
            jdbc.sql(
                            """
                    INSERT INTO outbox_event(
                        event_id, aggregate_id, event_type, payload_json, status, created_at)
                    VALUES(
                        :eventId, :aggregateId, 'RETURN_ITEM_RESTOCK_REQUESTED',
                        CAST(:payload AS jsonb), 'PENDING', :createdAt)
                    """)
                    .param("eventId", UUID.randomUUID().toString())
                    .param("aggregateId", returnKey)
                    .param("payload", outboxPayloadCodec.write(payload))
                    .param("createdAt", LocalDateTime.now())
                    .update();
        }
    }

    private boolean claim(String eventId) {
        try {
            jdbc.sql(
                            """
                    INSERT INTO inbox_event(consumer_name, event_id, processed_at)
                    VALUES('return-refund-v2', :eventId, :processedAt)
                    """)
                    .param("eventId", eventId)
                    .param("processedAt", LocalDateTime.now())
                    .update();
            return true;
        } catch (DuplicateKeyException duplicate) {
            return false;
        }
    }
}
