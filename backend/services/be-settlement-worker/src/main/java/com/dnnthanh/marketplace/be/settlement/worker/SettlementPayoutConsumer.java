package com.dnnthanh.marketplace.be.settlement.worker;

import com.dnnthanh.marketplace.be.platform.event.DomainEvent;
import com.dnnthanh.marketplace.be.platform.kafka.BaseDomainEventConsumer;
import com.dnnthanh.marketplace.be.platform.outbox.OutboxPayloadCodec;
import com.dnnthanh.marketplace.be.platform.stereotype.Adapter;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.transaction.annotation.Transactional;

/** Executes idempotent local payout simulation and records the automated transition for audit. */
@Adapter
@RequiredArgsConstructor
public class SettlementPayoutConsumer extends BaseDomainEventConsumer {

    private final JdbcClient jdbc;

    private final OutboxPayloadCodec outboxPayloadCodec;

    /**
     * Marks one requested payout PAID exactly once and emits a source-owned audit event.
     *
     * @param event typed settlement event
     */
    @KafkaListener(topics = "marketplace.settlement.events", groupId = "settlement-payout-v2")
    @Transactional
    public void onSettlement(DomainEvent event) {
        if (!accepts(event, "SETTLEMENT_PAYOUT_REQUESTED") || !claim(event.eventId())) {
            return;
        }

        String settlementNo = requiredString(event, "settlementNo");
        int changed =
                jdbc.sql(
                                """
                UPDATE settlement
                SET status='PAID', updated_at=SYSTIMESTAMP
                WHERE settlement_no=:settlementNo
                  AND status='PAYOUT_PENDING'
                """)
                        .param("settlementNo", settlementNo)
                        .update();

        if (changed > 0) {
            insertAudit(settlementNo);
        }
    }

    private boolean claim(String eventId) {
        try {
            jdbc.sql(
                            """
                    INSERT INTO inbox_event(consumer_name, event_id, processed_at)
                    VALUES('settlement-payout-v2', :eventId, SYSTIMESTAMP)
                    """)
                    .param("eventId", eventId)
                    .update();
            return true;
        } catch (DuplicateKeyException duplicate) {
            return false;
        }
    }

    private void insertAudit(String settlementNo) {
        String payload =
                outboxPayloadCodec.write(
                        Map.of(
                                "actorId", "be-settlement-worker",
                                "actorType", "SERVICE",
                                "action", "SETTLEMENT_PAID",
                                "resourceType", "SETTLEMENT",
                                "resourceId", settlementNo,
                                "sourceService", "be-settlement-worker"));

        jdbc.sql(
                        """
                INSERT INTO outbox_event(
                    event_id, aggregate_id, event_type, payload_json, status, created_at)
                VALUES(
                    :eventId, :aggregateId, 'AUDIT_EVENT', :payload, 'PENDING', SYSTIMESTAMP)
                """)
                .param("eventId", UUID.randomUUID().toString())
                .param("aggregateId", settlementNo)
                .param("payload", payload)
                .update();
    }
}
