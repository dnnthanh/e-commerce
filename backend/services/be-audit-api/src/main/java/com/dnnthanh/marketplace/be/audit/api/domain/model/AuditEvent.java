package com.dnnthanh.marketplace.be.audit.api.domain.model;

import com.dnnthanh.marketplace.be.audit.api.domain.enumtype.AuditActorType;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Immutable audit event with deterministic sensitive-field redaction. */
public record AuditEvent(
        String eventId,
        String actorId,
        AuditActorType actorType,
        String action,
        String resourceType,
        String resourceId,
        String sourceService,
        String traceId,
        LocalDateTime occurredAt,
        Map<String, Object> details) {
    private static final Set<String> SENSITIVE_KEYS =
            Set.of(
                    "password",
                    "token",
                    "authorization",
                    "cookie",
                    "cvv",
                    "clientSecret",
                    "refreshToken");

    public AuditEvent {
        Objects.requireNonNull(eventId);
        Objects.requireNonNull(actorType);
        Objects.requireNonNull(action);
        Objects.requireNonNull(resourceType);
        Objects.requireNonNull(resourceId);
        Objects.requireNonNull(sourceService);
        Objects.requireNonNull(occurredAt);
        details = redact(details);
    }

    private static Map<String, Object> redact(Map<String, Object> input) {
        if (input == null || input.isEmpty()) return Map.of();
        Map<String, Object> safe = new LinkedHashMap<>();
        input.forEach(
                (key, value) ->
                        safe.put(
                                key,
                                SENSITIVE_KEYS.stream()
                                                .anyMatch(
                                                        sensitive ->
                                                                sensitive.equalsIgnoreCase(key))
                                        ? "***"
                                        : value));
        return Map.copyOf(safe);
    }
}
