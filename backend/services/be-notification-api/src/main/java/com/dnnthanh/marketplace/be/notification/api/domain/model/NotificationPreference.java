package com.dnnthanh.marketplace.be.notification.api.domain.model;

import com.dnnthanh.marketplace.be.notification.api.domain.enumtype.NotificationChannel;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

/** User notification preference including channel opt-out and quiet hours. */
public final class NotificationPreference {
    private final String userId;
    private final Set<NotificationChannel> enabledChannels;
    private final LocalTime quietFrom;
    private final LocalTime quietUntil;

    public NotificationPreference(
            String userId,
            Set<NotificationChannel> enabledChannels,
            LocalTime quietFrom,
            LocalTime quietUntil) {
        this.userId = Objects.requireNonNull(userId);
        this.enabledChannels =
                enabledChannels == null
                        ? EnumSet.noneOf(NotificationChannel.class)
                        : EnumSet.copyOf(enabledChannels);
        this.quietFrom = quietFrom;
        this.quietUntil = quietUntil;
    }

    public boolean mayDeliver(NotificationChannel channel, LocalTime localTime) {
        if (!enabledChannels.contains(channel)) {
            return false;
        }
        if (channel == NotificationChannel.IN_APP || quietFrom == null || quietUntil == null) {
            return true;
        }
        boolean quiet =
                quietFrom.isBefore(quietUntil)
                        ? !localTime.isBefore(quietFrom) && localTime.isBefore(quietUntil)
                        : !localTime.isBefore(quietFrom) || localTime.isBefore(quietUntil);
        return !quiet;
    }

    public String userId() {
        return userId;
    }

    public Set<NotificationChannel> enabledChannels() {
        return Set.copyOf(enabledChannels);
    }

    public LocalTime quietFrom() {
        return quietFrom;
    }

    public LocalTime quietUntil() {
        return quietUntil;
    }
}
