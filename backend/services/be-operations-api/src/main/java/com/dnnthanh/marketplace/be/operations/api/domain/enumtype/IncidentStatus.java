package com.dnnthanh.marketplace.be.operations.api.domain.enumtype;

import com.dnnthanh.marketplace.be.platform.model.CodeEnum;

/** Operational incident lifecycle. */
public enum IncidentStatus implements CodeEnum {
    OPEN,
    RECOVERY_REQUESTED,
    RECOVERING,
    RESOLVED,
    IGNORED
}
