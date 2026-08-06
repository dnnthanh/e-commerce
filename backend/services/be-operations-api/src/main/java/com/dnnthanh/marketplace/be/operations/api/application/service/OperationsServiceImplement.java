package com.dnnthanh.marketplace.be.operations.api.application.service;

import com.dnnthanh.marketplace.be.operations.api.application.port.in.OperationsUseCase;
import com.dnnthanh.marketplace.be.operations.api.application.port.out.OperationsPersistencePort;
import com.dnnthanh.marketplace.be.operations.api.application.query.IncidentQueryResult;
import com.dnnthanh.marketplace.be.platform.context.UserContext;
import com.dnnthanh.marketplace.be.platform.stereotype.UseCase;
import java.util.List;
import lombok.RequiredArgsConstructor;

/** Coordinates incident inspection and auditable, idempotent recovery commands. */
@UseCase
@RequiredArgsConstructor
public class OperationsServiceImplement implements OperationsUseCase {
    private static final int OPEN_INCIDENT_LIMIT = 200;
    private final OperationsPersistencePort persistence;
    private final UserContext user;

    @Override
    public List<IncidentQueryResult> open() {
        return persistence.findOpen(OPEN_INCIDENT_LIMIT);
    }

    @Override
    public void recover(Long id, String reason) {
        persistence.requestRecovery(id, user.userId(), requireReason(reason));
    }

    @Override
    public void resolve(Long id, String reason) {
        persistence.resolve(id, user.userId(), requireReason(reason));
    }

    private static String requireReason(String reason) {
        if (reason == null || reason.isBlank()) {
            throw new InvalidOperationsCommandException(
                    "Operations command requires an audit reason");
        }
        return reason;
    }

    /** Invalid privileged operations command. */
    public static final class InvalidOperationsCommandException extends RuntimeException {
        public InvalidOperationsCommandException(String message) {
            super(message);
        }
    }
}
