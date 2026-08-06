package com.dnnthanh.marketplace.be.platform.event;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

/** Converts a durable relational/Mongo outbox row into the standard Kafka domain-event envelope. */
@Component
@RequiredArgsConstructor
public class OutboxEventFactory {

    private final ObjectMapper objectMapper;

    public DomainEvent fromOutbox(
            String eventId, String eventType, String aggregateId, String payloadJson) {
        try {
            Map<String, Object> payload =
                    payloadJson == null || payloadJson.isBlank()
                            ? new LinkedHashMap<>()
                            : objectMapper.readValue(payloadJson, new TypeReference<>() {});
            return new DomainEvent(
                    eventId,
                    eventType,
                    aggregateId,
                    LocalDateTime.now(),
                    stringValue(payload.get("traceId")),
                    stringValue(payload.get("correlationId")),
                    stringValue(payload.get("causationId")),
                    Map.copyOf(payload));
        } catch (Exception parseFailure) {
            throw new IllegalArgumentException(
                    "Invalid outbox payload for event " + eventId, parseFailure);
        }
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
