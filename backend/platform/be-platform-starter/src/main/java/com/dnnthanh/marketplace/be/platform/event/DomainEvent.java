package com.dnnthanh.marketplace.be.platform.event;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standard domain-event envelope exchanged through Kafka.
 *
 * @param eventId globally unique event identifier
 * @param eventType stable event type name
 * @param aggregateId business aggregate identifier used as Kafka key
 * @param occurredAt event occurrence time
 * @param traceId distributed trace identifier, when available
 * @param correlationId business-flow correlation identifier, when available
 * @param causationId identifier of the command/event that caused this event, when available
 * @param payload event-specific payload
 */
public record DomainEvent(
        String eventId,
        String eventType,
        String aggregateId,
        LocalDateTime occurredAt,
        String traceId,
        String correlationId,
        String causationId,
        Map<String, Object> payload) {}
