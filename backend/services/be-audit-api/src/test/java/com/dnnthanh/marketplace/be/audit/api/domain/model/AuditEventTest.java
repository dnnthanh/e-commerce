package com.dnnthanh.marketplace.be.audit.api.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.dnnthanh.marketplace.be.audit.api.domain.enumtype.AuditActorType;
import java.time.LocalDateTime;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AuditEventTest {
    @Test
    void redactsSensitiveDetailsBeforePersistence() {
        AuditEvent e =
                new AuditEvent(
                        "E",
                        "U",
                        AuditActorType.USER,
                        "UPDATE",
                        "USER",
                        "U",
                        "auth",
                        "T",
                        LocalDateTime.now(),
                        Map.of("token", "secret", "reason", "rotate"));
        assertEquals("***", e.details().get("token"));
        assertEquals("rotate", e.details().get("reason"));
    }
}
