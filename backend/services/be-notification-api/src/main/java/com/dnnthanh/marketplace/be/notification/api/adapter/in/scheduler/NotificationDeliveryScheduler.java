package com.dnnthanh.marketplace.be.notification.api.adapter.in.scheduler;

import com.dnnthanh.marketplace.be.notification.api.application.port.in.NotificationDeliveryUseCase;
import com.dnnthanh.marketplace.be.platform.stereotype.Adapter;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

/** Pull-based durable delivery worker. Mongo claim/lease makes multiple API instances safe. */
@Slf4j
@Adapter
@RequiredArgsConstructor
public class NotificationDeliveryScheduler {
    private static final int DEFAULT_BATCH_SIZE = 100;
    private final NotificationDeliveryUseCase deliveryUseCase;

    @Scheduled(fixedDelayString = "${notification.delivery.poll-delay-ms:1000}")
    public void deliverDue() {
        var result = deliveryUseCase.deliverDue(LocalDateTime.now(), DEFAULT_BATCH_SIZE);
        if (result.delivered() + result.retried() + result.deadLettered() > 0) {
            log.info(
                    "notification_delivery_batch delivered={} retried={} deadLettered={}",
                    result.delivered(),
                    result.retried(),
                    result.deadLettered());
        }
    }
}
