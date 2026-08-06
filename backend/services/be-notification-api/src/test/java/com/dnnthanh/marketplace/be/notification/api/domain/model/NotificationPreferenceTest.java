package com.dnnthanh.marketplace.be.notification.api.domain.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dnnthanh.marketplace.be.notification.api.domain.enumtype.NotificationChannel;
import java.time.LocalTime;
import java.util.EnumSet;
import org.junit.jupiter.api.Test;

class NotificationPreferenceTest {
    @Test
    void suppressesExternalChannelDuringQuietHoursButKeepsInbox() {
        NotificationPreference p =
                new NotificationPreference(
                        "U",
                        EnumSet.of(NotificationChannel.EMAIL, NotificationChannel.IN_APP),
                        LocalTime.of(22, 0),
                        LocalTime.of(7, 0));
        assertFalse(p.mayDeliver(NotificationChannel.EMAIL, LocalTime.of(23, 0)));
        assertTrue(p.mayDeliver(NotificationChannel.IN_APP, LocalTime.of(23, 0)));
    }
}
