package com.dnnthanh.marketplace.be.authorization.api.application;

import com.dnnthanh.marketplace.be.authorization.api.application.port.out.AuthorizationChangePort;
import com.dnnthanh.marketplace.be.authorization.api.domain.model.AuthorizationChangeType;
import com.dnnthanh.marketplace.be.platform.stereotype.UseCase;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;

/** Durable authorization-mutation journal used before and after identity-provider I/O. */
@UseCase
@RequiredArgsConstructor
public class AuthorizationChangeRecorder {
    private final AuthorizationChangePort changePort;

    public String prepareChange(
            String userId,
            AuthorizationChangeType type,
            String actor,
            Map<String, Object> details) {
        return changePort.prepareChange(userId, type, actor, details);
    }

    public void markApplied(String changeId) {
        changePort.markApplied(changeId);
    }

    public void markFailed(String changeId, RuntimeException failure) {
        String message =
                failure.getMessage() == null
                        ? failure.getClass().getSimpleName()
                        : failure.getMessage();
        changePort.markFailed(changeId, message);
    }

    public List<AuthorizationChangePort.PendingChange> pending(int limit) {
        return changePort.findPending(limit);
    }
}
