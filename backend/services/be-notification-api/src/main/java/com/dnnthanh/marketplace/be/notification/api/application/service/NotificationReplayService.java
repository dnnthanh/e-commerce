package com.dnnthanh.marketplace.be.notification.api.application.service;

import com.dnnthanh.marketplace.be.notification.api.application.port.out.NotificationInboxPort;
import com.dnnthanh.marketplace.be.platform.stereotype.UseCase;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;

/** Guarded operations use case that re-queues one dead-lettered notification. */
@UseCase
@RequiredArgsConstructor
public class NotificationReplayService {
    private final NotificationInboxPort inbox;

    public boolean replay(String notificationId) {
        return inbox.findDelivery(notificationId)
                .filter(
                        delivery ->
                                delivery.status()
                                        == com.dnnthanh.marketplace.be.notification.api.domain.model
                                                .NotificationDelivery.DeliveryStatus.DEAD_LETTER)
                .map(
                        delivery -> {
                            delivery.replay(LocalDateTime.now());
                            inbox.save(delivery);
                            return true;
                        })
                .orElse(false);
    }
}
