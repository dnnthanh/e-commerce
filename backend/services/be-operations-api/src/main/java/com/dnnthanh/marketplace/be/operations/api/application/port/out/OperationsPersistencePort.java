package com.dnnthanh.marketplace.be.operations.api.application.port.out;

import com.dnnthanh.marketplace.be.operations.api.application.query.IncidentQueryResult;
import java.util.List;

/** Bounded incident query and auditable recovery persistence boundary. */
public interface OperationsPersistencePort {
    List<IncidentQueryResult> findOpen(int limit);

    void requestRecovery(Long incidentId, String requestedBy, String reason);

    boolean resolve(Long incidentId, String requestedBy, String reason);
}
