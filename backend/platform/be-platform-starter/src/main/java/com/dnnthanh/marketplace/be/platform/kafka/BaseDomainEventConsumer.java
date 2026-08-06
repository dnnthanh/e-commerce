package com.dnnthanh.marketplace.be.platform.kafka;

import com.dnnthanh.marketplace.be.platform.event.DomainEvent;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

/**
 * Base Kafka consumer helper for the standard typed {@link DomainEvent} envelope. Subclasses keep
 * only topic annotations and business handling; transport validation stays centralized.
 */
@Slf4j
public abstract class BaseDomainEventConsumer {

    /**
     * Validates the event envelope and checks whether its type is handled by the current listener.
     *
     * @param event typed domain event supplied by Spring Kafka
     * @param supportedEventTypes event types handled by the listener
     * @return {@code true} when business handling should continue
     */
    protected final boolean accepts(DomainEvent event, String... supportedEventTypes) {
        Objects.requireNonNull(event, "event");
        Objects.requireNonNull(event.eventId(), "event.eventId");
        Objects.requireNonNull(event.eventType(), "event.eventType");
        Set<String> supported =
                Arrays.stream(supportedEventTypes).collect(Collectors.toUnmodifiableSet());
        boolean accepted = supported.contains(event.eventType());
        if (!accepted) {
            log.debug(
                    "kafka_event_ignored consumer={} eventId={} eventType={} supported={}",
                    getClass().getSimpleName(),
                    event.eventId(),
                    event.eventType(),
                    supported);
        }
        return accepted;
    }

    /**
     * Returns the typed event payload map, never a raw JSON string.
     *
     * @param event typed domain event
     * @return non-null payload map
     */
    protected final Map<String, Object> payload(DomainEvent event) {
        return event.payload() == null ? Map.of() : event.payload();
    }

    /**
     * Reads a required string field from the typed payload.
     *
     * @param event typed domain event
     * @param field payload field name
     * @return required string value
     */
    protected final String requiredString(DomainEvent event, String field) {
        Object value = payload(event).get(field);
        if (value == null || String.valueOf(value).isBlank()) {
            throw new IllegalArgumentException(
                    "Missing Kafka payload field: " + field + " for " + event.eventId());
        }
        return String.valueOf(value);
    }

    /**
     * Reads a required long field from the typed payload.
     *
     * @param event typed domain event
     * @param field payload field name
     * @return required long value
     */
    protected final long requiredLong(DomainEvent event, String field) {
        Object value = payload(event).get(field);
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(requiredString(event, field));
        } catch (NumberFormatException failure) {
            throw new IllegalArgumentException(
                    "Kafka payload field is not a long: " + field, failure);
        }
    }

    /**
     * Runs one typed event through the common validation and failure-logging template.
     *
     * @param event typed domain event supplied by Spring Kafka
     * @param handler bounded-context event handler
     * @param supportedEventTypes event types handled by the listener
     */
    protected final void consume(
            DomainEvent event,
            java.util.function.Consumer<DomainEvent> handler,
            String... supportedEventTypes) {
        if (!accepts(event, supportedEventTypes)) {
            return;
        }
        try {
            handler.accept(event);
        } catch (RuntimeException failure) {
            log.warn(
                    "kafka_consume_failed consumer={} eventId={} eventType={} failure={}",
                    getClass().getSimpleName(),
                    event.eventId(),
                    event.eventType(),
                    failure.toString());
            throw failure;
        }
    }
}
