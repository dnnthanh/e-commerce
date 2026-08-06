package com.dnnthanh.marketplace.be.operations.api.application.port.in;

import com.dnnthanh.marketplace.be.operations.api.application.query.IncidentQueryResult;
import java.util.List;

/** Inbound application port for operations incident inspection and recovery. */
public interface OperationsUseCase {
    List<IncidentQueryResult> open();

    void recover(Long id, String reason);

    void resolve(Long id, String reason);
}
