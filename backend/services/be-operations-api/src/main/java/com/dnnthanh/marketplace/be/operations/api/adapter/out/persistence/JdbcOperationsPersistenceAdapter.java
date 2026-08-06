package com.dnnthanh.marketplace.be.operations.api.adapter.out.persistence;

import com.dnnthanh.marketplace.be.operations.api.application.port.out.OperationsPersistencePort;
import com.dnnthanh.marketplace.be.operations.api.application.query.IncidentQueryResult;
import com.dnnthanh.marketplace.be.operations.api.domain.enumtype.OperationsEventType;
import com.dnnthanh.marketplace.be.operations.api.domain.exception.OperationsIncidentNotFoundException;
import com.dnnthanh.marketplace.be.platform.stereotype.Persistence;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

/** PostgreSQL operations adapter; recovery request and audit outbox are written atomically. */
@Persistence
@RequiredArgsConstructor
public class JdbcOperationsPersistenceAdapter implements OperationsPersistencePort {
    private final JdbcClient jdbc;
    private final ObjectMapper json;

    @Override
    public List<IncidentQueryResult> findOpen(int limit) {
        return jdbc.sql(
                        """
            SELECT id,incident_type,source_service,recovery_target,aggregate_id,status,severity,
                last_error,first_seen_at
            FROM operations_incident
            WHERE status='OPEN'
            ORDER BY severity DESC,last_seen_at DESC
            LIMIT :limit
            """)
                .param("limit", limit)
                .query(
                        (rs, row) ->
                                new IncidentQueryResult(
                                        rs.getLong(1),
                                        rs.getString(2),
                                        rs.getString(3),
                                        rs.getString(4),
                                        rs.getString(5),
                                        rs.getString(6),
                                        rs.getString(7),
                                        rs.getString(8),
                                        rs.getTimestamp(9).toLocalDateTime()))
                .list();
    }

    @Override
    @Transactional
    public void requestRecovery(Long incidentId, String requestedBy, String reason) {
        Map<String, Object> incident =
                jdbc.sql(
                                """
            SELECT recovery_target,aggregate_id,incident_type
            FROM operations_incident
            WHERE id=:id AND status='OPEN'
            FOR UPDATE
            """)
                        .param("id", incidentId)
                        .query(new ColumnMapRowMapper())
                        .optional()
                        .orElseThrow(() -> new OperationsIncidentNotFoundException(incidentId));
        LocalDateTime now = LocalDateTime.now();
        String aggregateId = String.valueOf(incident.get("aggregate_id"));
        jdbc.sql(
                        """
            INSERT INTO recovery_attempt(incident_id,requested_by,action,status,message,created_at)
            VALUES(:id,:requestedBy,'RECOVER','REQUESTED',:reason,:createdAt)
            """)
                .param("id", incidentId)
                .param("requestedBy", requestedBy)
                .param("reason", reason)
                .param("createdAt", now)
                .update();
        insertOutbox(
                aggregateId,
                OperationsEventType.RECOVERY_REQUESTED,
                Map.of(
                        "incidentId", incidentId,
                        "target", String.valueOf(incident.get("recovery_target")),
                        "aggregateId", aggregateId,
                        "incidentType", String.valueOf(incident.get("incident_type")),
                        "requestedBy", requestedBy),
                now);
        insertAudit(
                aggregateId, incidentId, requestedBy, "OPERATIONS_RECOVERY_REQUESTED", reason, now);
    }

    @Override
    @Transactional
    public boolean resolve(Long incidentId, String requestedBy, String reason) {
        LocalDateTime now = LocalDateTime.now();
        int updated =
                jdbc.sql(
                                """
            UPDATE operations_incident
            SET status='RESOLVED',last_recovery_message=:reason,resolved_at=:at,last_seen_at=:at
            WHERE id=:id AND status <> 'RESOLVED'
            """)
                        .param("reason", reason)
                        .param("at", now)
                        .param("id", incidentId)
                        .update();
        if (updated == 0) return false;
        insertAudit(
                String.valueOf(incidentId),
                incidentId,
                requestedBy,
                "OPERATIONS_INCIDENT_RESOLVED",
                reason,
                now);
        return true;
    }

    private void insertAudit(
            String aggregateId,
            Long incidentId,
            String actor,
            String action,
            String reason,
            LocalDateTime now) {
        insertOutbox(
                aggregateId,
                OperationsEventType.AUDIT_EVENT,
                Map.of(
                        "actorId",
                        actor,
                        "actorType",
                        "USER",
                        "action",
                        action,
                        "resourceType",
                        "OPERATIONS_INCIDENT",
                        "resourceId",
                        String.valueOf(incidentId),
                        "sourceService",
                        "be-operations-api",
                        "reason",
                        reason),
                now);
    }

    private void insertOutbox(
            String aggregateId,
            OperationsEventType eventType,
            Map<String, Object> payload,
            LocalDateTime now) {
        try {
            jdbc.sql(
                            """
              INSERT INTO outbox_event(event_id,aggregate_id,event_type,payload_json,status,created_at)
              VALUES(:eventId,:aggregateId,:eventType,CAST(:payload AS jsonb),'PENDING',:createdAt)
              """)
                    .param("eventId", UUID.randomUUID().toString())
                    .param("aggregateId", aggregateId)
                    .param("eventType", eventType.name())
                    .param("payload", json.writeValueAsString(payload))
                    .param("createdAt", now)
                    .update();
        } catch (Exception failure) {
            throw new OperationsPersistenceException(
                    "Operations event cannot be serialized", failure);
        }
    }

    /** Named infrastructure failure. */
    public static final class OperationsPersistenceException extends RuntimeException {
        public OperationsPersistenceException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
