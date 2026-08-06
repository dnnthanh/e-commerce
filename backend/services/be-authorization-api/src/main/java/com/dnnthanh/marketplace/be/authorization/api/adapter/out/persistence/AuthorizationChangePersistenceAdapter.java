package com.dnnthanh.marketplace.be.authorization.api.adapter.out.persistence;

import com.dnnthanh.marketplace.be.authorization.api.adapter.out.persistence.entity.AuthorizationChangeJpaEntity;
import com.dnnthanh.marketplace.be.authorization.api.adapter.out.persistence.entity.AuthorizationOutboxJpaEntity;
import com.dnnthanh.marketplace.be.authorization.api.adapter.out.persistence.exception.AuthorizationPersistenceException;
import com.dnnthanh.marketplace.be.authorization.api.adapter.out.persistence.repository.AuthorizationChangeJpaRepository;
import com.dnnthanh.marketplace.be.authorization.api.adapter.out.persistence.repository.AuthorizationOutboxJpaRepository;
import com.dnnthanh.marketplace.be.authorization.api.application.port.out.AuthorizationChangePort;
import com.dnnthanh.marketplace.be.authorization.api.domain.model.AuthorizationChangeType;
import com.dnnthanh.marketplace.be.platform.stereotype.Persistence;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

/**
 * Durable authorization mutation persistence. Simple mutation CRUD/outbox writes use Spring Data
 * JPA; the operational pending-reconciliation list uses deterministic native SQL.
 */
@Persistence
@RequiredArgsConstructor
public class AuthorizationChangePersistenceAdapter implements AuthorizationChangePort {
    private static final String AUTHORIZATION_CHANGED = "AUTHORIZATION_CHANGED";
    private static final String AUDIT_EVENT = "AUDIT_EVENT";
    private static final String PENDING = "PENDING";

    private final AuthorizationChangeJpaRepository changes;
    private final AuthorizationOutboxJpaRepository outbox;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public String prepareChange(
            String userId,
            AuthorizationChangeType changeType,
            String actor,
            Map<String, Object> details) {
        String changeId = UUID.randomUUID().toString();
        AuthorizationChangeJpaEntity entity = new AuthorizationChangeJpaEntity();
        entity.setChangeId(changeId);
        entity.setUserId(userId);
        entity.setChangeType(changeType.name());
        entity.setChangedBy(actor);
        entity.setChangedAt(LocalDateTime.now());
        entity.setPayloadJson(json(details));
        entity.setStatus(PENDING);
        entity.setAttemptCount(0);
        changes.saveAndFlush(entity);
        return changeId;
    }

    @Override
    @Transactional
    public void markApplied(String changeId) {
        PendingChange change = load(changeId);
        if (changes.markApplied(changeId, LocalDateTime.now()) == 0) {
            return;
        }
        appendOutbox(
                change.userId(),
                AUTHORIZATION_CHANGED,
                Map.of(
                        "userId",
                        change.userId(),
                        "changeType",
                        change.changeType().name(),
                        "changedBy",
                        change.actor(),
                        "details",
                        change.details(),
                        "changeId",
                        change.changeId()));
        appendOutbox(
                change.userId(),
                AUDIT_EVENT,
                Map.of(
                        "actorId",
                        change.actor(),
                        "actorType",
                        "USER",
                        "action",
                        change.changeType().name(),
                        "resourceType",
                        "AUTHORIZATION",
                        "resourceId",
                        change.userId(),
                        "sourceService",
                        "be-authorization-api",
                        "after",
                        change.details(),
                        "changeId",
                        change.changeId()));
    }

    @Override
    @Transactional
    public void markFailed(String changeId, String error) {
        changes.markFailed(changeId, error, LocalDateTime.now());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PendingChange> findPending(int limit) {
        int boundedLimit = Math.max(1, Math.min(limit, 500));
        return changes.findPendingNative(boundedLimit).stream().map(this::toPendingChange).toList();
    }

    private PendingChange load(String changeId) {
        return changes.findByChangeId(changeId)
                .map(this::toPendingChange)
                .orElseThrow(
                        () ->
                                new AuthorizationPersistenceException(
                                        "Authorization change not found: " + changeId));
    }

    private PendingChange toPendingChange(AuthorizationChangeJpaEntity entity) {
        return new PendingChange(
                entity.getChangeId(),
                entity.getUserId(),
                AuthorizationChangeType.valueOf(entity.getChangeType()),
                entity.getChangedBy(),
                parse(entity.getPayloadJson()),
                entity.getAttemptCount());
    }

    private void appendOutbox(String aggregateId, String eventType, Map<String, Object> payload) {
        AuthorizationOutboxJpaEntity event = new AuthorizationOutboxJpaEntity();
        event.setEventId(UUID.randomUUID().toString());
        event.setAggregateId(aggregateId);
        event.setEventType(eventType);
        event.setPayloadJson(json(payload));
        event.setStatus(PENDING);
        event.setCreatedAt(LocalDateTime.now());
        outbox.save(event);
    }

    private String json(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception failure) {
            throw new AuthorizationPersistenceException(
                    "Authorization payload cannot be serialized", failure);
        }
    }

    private Map<String, Object> parse(String payload) {
        try {
            return objectMapper.readValue(payload, new TypeReference<Map<String, Object>>() {});
        } catch (Exception failure) {
            throw new AuthorizationPersistenceException(
                    "Authorization payload cannot be read", failure);
        }
    }
}
