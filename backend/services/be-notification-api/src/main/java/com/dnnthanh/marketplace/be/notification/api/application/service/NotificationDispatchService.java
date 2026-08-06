package com.dnnthanh.marketplace.be.notification.api.application.service;

import com.dnnthanh.marketplace.be.notification.api.application.port.out.NotificationInboxPort;
import com.dnnthanh.marketplace.be.notification.api.domain.enumtype.NotificationChannel;
import com.dnnthanh.marketplace.be.notification.api.domain.model.NotificationDelivery;
import com.dnnthanh.marketplace.be.platform.model.CodeEnum;
import com.dnnthanh.marketplace.be.platform.stereotype.UseCase;
import java.time.LocalTime;
import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class NotificationDispatchService {
    private final NotificationInboxPort inboxPort;

    public DispatchDecision prepare(
            String notificationId,
            String deduplicationKey,
            String userId,
            NotificationChannel channel,
            LocalTime userLocalTime) {
        if (inboxPort.findByDeduplicationKey(deduplicationKey).isPresent())
            return DispatchDecision.DUPLICATE;
        boolean allowed =
                inboxPort
                        .preference(userId)
                        .map(preference -> preference.mayDeliver(channel, userLocalTime))
                        .orElse(channel == NotificationChannel.IN_APP);
        if (!allowed) return DispatchDecision.SUPPRESSED;
        inboxPort.save(new NotificationDelivery(notificationId, deduplicationKey, userId, channel));
        return DispatchDecision.CREATED;
    }

    public enum DispatchDecision implements CodeEnum {
        CREATED,
        DUPLICATE,
        SUPPRESSED
    }
}
