package com.dnnthanh.marketplace.be.operations.worker;

import com.dnnthanh.marketplace.be.platform.event.DomainEvent;
import com.dnnthanh.marketplace.be.platform.kafka.BaseDomainEventConsumer;
import com.dnnthanh.marketplace.be.platform.stereotype.Adapter;
import java.time.LocalDateTime;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.transaction.annotation.Transactional;

/** Materializes durable operational incidents from ambiguous payment events. */
@Adapter
public class PaymentIncidentConsumer extends BaseDomainEventConsumer {

    private final JdbcClient jdbc;

    public PaymentIncidentConsumer(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    /** Upserts PAYMENT_UNKNOWN as an actionable incident, idempotently. */
    @Transactional
    @KafkaListener(topics = "marketplace.payment.events", groupId = "operations-payment-v1")
    public void onPayment(DomainEvent event) {
        if (!accepts(event, "PAYMENT_UNKNOWN")) return;
        if (!claim(event.eventId())) return;
        String paymentKey = String.valueOf(event.payload().get("paymentKey"));
        LocalDateTime now = LocalDateTime.now();
        jdbc.sql(
                        """
                INSERT INTO operations_incident(
                    incident_key,incident_type,source_service,recovery_target,aggregate_id,status,severity,
                    last_error,last_recovery_message,first_seen_at,last_seen_at,resolved_at)
                VALUES(:key,'PAYMENT_UNKNOWN','be-payment-api','be-payment-worker',:aggregate,'OPEN','HIGH',
                       'Provider outcome is ambiguous',NULL,:now,:now,NULL)
                ON CONFLICT(incident_key) DO UPDATE SET
                    status='OPEN',last_seen_at=EXCLUDED.last_seen_at,last_error=EXCLUDED.last_error,resolved_at=NULL
                """)
                .param("key", "PAYMENT_UNKNOWN:" + paymentKey)
                .param("aggregate", paymentKey)
                .param("now", now)
                .update();
    }

    private boolean claim(String eventId) {
        try {
            jdbc.sql(
                            "INSERT INTO inbox_event(consumer_name,event_id,processed_at) VALUES('operations-payment-v1',:eventId,:at)")
                    .param("eventId", eventId)
                    .param("at", LocalDateTime.now())
                    .update();
            return true;
        } catch (DuplicateKeyException duplicate) {
            return false;
        }
    }
}
