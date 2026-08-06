package com.dnnthanh.marketplace.be.operations.api.domain.model;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.dnnthanh.marketplace.be.operations.api.domain.enumtype.RecoveryAction;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class OperationalIncidentTest {
    @Test
    void deduplicatesRecoveryAndRejectsUnsafeRetry() {
        OperationalIncident i = new OperationalIncident(1L, "O", "be-order-api");
        var a =
                i.requestRecovery(
                        "R1",
                        RecoveryAction.RECONCILE,
                        "admin",
                        "provider mismatch",
                        true,
                        LocalDateTime.now());
        assertSame(
                a,
                i.requestRecovery(
                        "R1",
                        RecoveryAction.RECONCILE,
                        "admin",
                        "provider mismatch",
                        true,
                        LocalDateTime.now()));
        OperationalIncident unsafe = new OperationalIncident(2L, "O2", "be-payment-api");
        assertThrows(
                OperationalIncident.UnsafeRecoveryException.class,
                () ->
                        unsafe.requestRecovery(
                                "R2",
                                RecoveryAction.RETRY_IDEMPOTENT_COMMAND,
                                "admin",
                                "retry",
                                false,
                                LocalDateTime.now()));
    }
}
