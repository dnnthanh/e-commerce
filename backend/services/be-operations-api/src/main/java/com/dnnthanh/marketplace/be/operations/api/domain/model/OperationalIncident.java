package com.dnnthanh.marketplace.be.operations.api.domain.model;

import com.dnnthanh.marketplace.be.operations.api.domain.enumtype.IncidentStatus;
import com.dnnthanh.marketplace.be.operations.api.domain.enumtype.RecoveryAction;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Guarded operations aggregate; every recovery attempt is explicit and auditable. */
public final class OperationalIncident {
    private final Long incidentId;
    private final String aggregateId;
    private final String sourceService;
    private final List<RecoveryAttempt> attempts = new ArrayList<>();
    private IncidentStatus status = IncidentStatus.OPEN;

    public OperationalIncident(Long incidentId, String aggregateId, String sourceService) {
        this.incidentId = Objects.requireNonNull(incidentId);
        this.aggregateId = Objects.requireNonNull(aggregateId);
        this.sourceService = Objects.requireNonNull(sourceService);
    }

    public RecoveryAttempt requestRecovery(
            String requestKey,
            RecoveryAction action,
            String actorId,
            String reason,
            boolean actionIsIdempotent,
            LocalDateTime at) {
        if (status == IncidentStatus.RESOLVED || status == IncidentStatus.IGNORED) {
            throw new OperationsStateConflictException("Closed incident cannot be recovered");
        }
        RecoveryAttempt existing =
                attempts.stream()
                        .filter(a -> a.requestKey().equals(requestKey))
                        .findFirst()
                        .orElse(null);
        if (existing != null) return existing;
        if (!actionIsIdempotent && action == RecoveryAction.RETRY_IDEMPOTENT_COMMAND) {
            throw new UnsafeRecoveryException(
                    "Retry requested for a command that is not idempotent");
        }
        if (reason == null || reason.isBlank())
            throw new UnsafeRecoveryException("Recovery requires an audit reason");
        RecoveryAttempt created = new RecoveryAttempt(requestKey, action, actorId, reason, at);
        attempts.add(created);
        status = IncidentStatus.RECOVERY_REQUESTED;
        return created;
    }

    public void markRecovering() {
        require(IncidentStatus.RECOVERY_REQUESTED);
        status = IncidentStatus.RECOVERING;
    }

    public void resolve() {
        if (status == IncidentStatus.RESOLVED) return;
        if (status == IncidentStatus.IGNORED)
            throw new OperationsStateConflictException(
                    "Ignored incident cannot be resolved by recovery");
        status = IncidentStatus.RESOLVED;
    }

    private void require(IncidentStatus expected) {
        if (status != expected)
            throw new OperationsStateConflictException(
                    "Expected " + expected + " but was " + status);
    }

    public IncidentStatus status() {
        return status;
    }

    public List<RecoveryAttempt> attempts() {
        return List.copyOf(attempts);
    }

    public record RecoveryAttempt(
            String requestKey,
            RecoveryAction action,
            String actorId,
            String reason,
            LocalDateTime requestedAt) {}

    public static final class UnsafeRecoveryException extends RuntimeException {
        public UnsafeRecoveryException(String message) {
            super(message);
        }
    }

    public static final class OperationsStateConflictException extends RuntimeException {
        public OperationsStateConflictException(String message) {
            super(message);
        }
    }
}
