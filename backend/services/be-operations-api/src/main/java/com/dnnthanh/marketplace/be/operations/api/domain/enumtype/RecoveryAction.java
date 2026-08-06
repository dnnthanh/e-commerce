package com.dnnthanh.marketplace.be.operations.api.domain.enumtype;

import com.dnnthanh.marketplace.be.platform.model.CodeEnum;

/** Explicit supported recovery operations; there is intentionally no arbitrary SQL action. */
public enum RecoveryAction implements CodeEnum {
    REPLAY_EVENT,
    RECONCILE,
    RETRY_IDEMPOTENT_COMMAND,
    RESUME_SAGA
}
