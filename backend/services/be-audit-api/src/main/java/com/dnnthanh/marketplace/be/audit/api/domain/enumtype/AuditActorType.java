package com.dnnthanh.marketplace.be.audit.api.domain.enumtype;

import com.dnnthanh.marketplace.be.platform.model.CodeEnum;

/** Actor origin for immutable audit history. */
public enum AuditActorType implements CodeEnum {
    USER,
    SERVICE,
    SCHEDULER,
    KAFKA_CONSUMER
}
