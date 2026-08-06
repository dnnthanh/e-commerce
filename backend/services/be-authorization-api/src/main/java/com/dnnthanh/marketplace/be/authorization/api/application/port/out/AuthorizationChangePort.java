package com.dnnthanh.marketplace.be.authorization.api.application.port.out;

import com.dnnthanh.marketplace.be.authorization.api.domain.model.AuthorizationChangeType;
import java.util.List;
import java.util.Map;

/** Durable privileged-mutation intent, completion and reconciliation boundary. */
public interface AuthorizationChangePort {
    String prepareChange(
            String userId,
            AuthorizationChangeType changeType,
            String actor,
            Map<String, Object> details);

    void markApplied(String changeId);

    void markFailed(String changeId, String error);

    List<PendingChange> findPending(int limit);

    record PendingChange(
            String changeId,
            String userId,
            AuthorizationChangeType changeType,
            String actor,
            Map<String, Object> details,
            int attemptCount) {}
}
