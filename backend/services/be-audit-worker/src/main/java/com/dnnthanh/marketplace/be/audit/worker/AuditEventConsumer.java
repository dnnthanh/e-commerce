package com.dnnthanh.marketplace.be.audit.worker;

import com.dnnthanh.marketplace.be.audit.worker.exception.InvalidAuditEventException;
import com.dnnthanh.marketplace.be.platform.event.DomainEvent;
import com.dnnthanh.marketplace.be.platform.kafka.BaseDomainEventConsumer;
import com.dnnthanh.marketplace.be.platform.stereotype.Adapter;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.kafka.annotation.KafkaListener;

/** Persists immutable typed audit events without coupling source services to the audit database. */
@Adapter
@RequiredArgsConstructor
public class AuditEventConsumer extends BaseDomainEventConsumer {

    private final JdbcClient jdbc;

    /**
     * Persists one source-owned audit event idempotently.
     *
     * @param event standard typed Kafka event
     */
    @KafkaListener(topics = "marketplace.audit.events", groupId = "be-audit-worker-v2")
    public void consume(DomainEvent event) {
        if (!accepts(event, "AUDIT_EVENT")) {
            return;
        }
        Map<String, Object> payload = event.payload();
        jdbc.sql(
                        """
            INSERT INTO audit_event(
                event_id,actor_id,actor_type,action,resource_type,resource_id,source_service,
                trace_id,before_json,after_json,reason,occurred_at)
            VALUES(
                :event,:actor,:actorType,:action,:resource,:resourceId,:source,
                :trace,CAST(:before AS jsonb),CAST(:after AS jsonb),:reason,:at)
            ON CONFLICT(event_id) DO NOTHING
            """)
                .param("event", event.eventId())
                .param("actor", string(payload.get("actorId"), "system"))
                .param("actorType", string(payload.get("actorType"), "SERVICE"))
                .param("action", required(payload, "action"))
                .param("resource", required(payload, "resourceType"))
                .param("resourceId", nullable(payload.get("resourceId")))
                .param("source", required(payload, "sourceService"))
                .param("trace", event.traceId())
                .param("before", jsonValue(payload.get("before")))
                .param("after", jsonValue(payload.get("after")))
                .param("reason", nullable(payload.get("reason")))
                .param("at", event.occurredAt() == null ? LocalDateTime.now() : event.occurredAt())
                .update();
    }

    private static String required(Map<String, Object> payload, String key) {
        Object value = payload.get(key);
        if (value == null || String.valueOf(value).isBlank()) {
            throw new InvalidAuditEventException("Audit payload is missing " + key);
        }
        return String.valueOf(value);
    }

    private static String string(Object value, String fallback) {
        return value == null || String.valueOf(value).isBlank() ? fallback : String.valueOf(value);
    }

    private static String nullable(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private static String jsonValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String text && (text.startsWith("{") || text.startsWith("["))) {
            return text;
        }
        return "\"" + String.valueOf(value).replace("\"", "\\\"") + "\"";
    }
}
