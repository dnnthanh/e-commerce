package com.dnnthanh.marketplace.be.notification.api.application.port.in;

import java.time.LocalDateTime;

/** Input port for durable notification provider delivery. */
public interface NotificationDeliveryUseCase {

    DeliveryBatchResult deliverDue(LocalDateTime now, int requestedLimit);

    record DeliveryBatchResult(int delivered, int retried, int deadLettered) {}
}
