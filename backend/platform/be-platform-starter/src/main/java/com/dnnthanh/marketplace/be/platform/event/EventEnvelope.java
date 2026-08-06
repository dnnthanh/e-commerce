package com.dnnthanh.marketplace.be.platform.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Durable integration-event envelope shared only for technical transport concerns.
 *
 * @param eventId unique event identifier used for idempotency
 * @param eventType stable event type name
 * @param eventVersion schema version
 * @param aggregateId business aggregate identifier
 * @param occurredAt local application timestamp
 * @param traceId distributed trace identifier
 * @param correlationId business correlation identifier
 * @param causationId event/request that caused this event
 * @param payload typed event payload
 * @param <T> event payload type
 */
public record EventEnvelope<T>(
        UUID eventId,
        String eventType,
        int eventVersion,
        String aggregateId,
        LocalDateTime occurredAt,
        String traceId,
        String correlationId,
        String causationId,
        T payload) {
    public static <T> EventEnvelope<T> create(
            String eventType, String aggregateId, String traceId, T payload) {
        return new EventEnvelope<>(
                UUID.randomUUID(),
                eventType,
                1,
                aggregateId,
                LocalDateTime.now(),
                traceId,
                traceId,
                null,
                payload);
    }
}
