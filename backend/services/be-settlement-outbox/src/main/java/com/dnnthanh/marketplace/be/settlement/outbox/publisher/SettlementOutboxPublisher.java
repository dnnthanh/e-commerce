package com.dnnthanh.marketplace.be.settlement.outbox.publisher;

import com.dnnthanh.marketplace.be.platform.event.DomainEvent;
import com.dnnthanh.marketplace.be.platform.event.OutboxEventFactory;
import com.dnnthanh.marketplace.be.platform.kafka.DomainEventProducer;
import com.dnnthanh.marketplace.be.platform.stereotype.Adapter;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Independently scalable settlement outbox publisher. Business transactions only persist durable
 * rows; this process owns Kafka network I/O.
 */
@Adapter
@RequiredArgsConstructor
public class SettlementOutboxPublisher {

    private final JdbcClient jdbc;

    private final DomainEventProducer kafka;

    private final OutboxEventFactory eventFactory;

    @Scheduled(fixedDelayString = "${outbox.poll-ms:1000}")
    public void publishBatch() {
        List<Map<String, Object>> rows =
                jdbc.sql(
                                """
                SELECT id,event_id,aggregate_id,event_type,payload_json FROM outbox_event WHERE status='PENDING' ORDER BY id FETCH FIRST 100 ROWS ONLY
                """)
                        .query()
                        .listOfRows();
        for (Map<String, Object> row : rows) {
            long id = ((Number) row.get("id")).longValue();
            String aggregateId = String.valueOf(row.get("aggregate_id"));
            DomainEvent event =
                    eventFactory.fromOutbox(
                            String.valueOf(row.get("event_id")),
                            String.valueOf(row.get("event_type")),
                            aggregateId,
                            String.valueOf(row.get("payload_json")));
            kafka.publish(
                            "AUDIT_EVENT".equals(event.eventType())
                                    ? "marketplace.audit.events"
                                    : "marketplace.settlement.events",
                            aggregateId,
                            event)
                    .whenComplete(
                            (result, error) -> {
                                if (error == null) {
                                    jdbc.sql(
                                                    """
                            UPDATE outbox_event SET status='PROCESSED',processed_at=SYSTIMESTAMP WHERE id=:id AND status='PENDING'
                            """)
                                            .param("id", id)
                                            .update();
                                }
                            });
        }
    }
}
