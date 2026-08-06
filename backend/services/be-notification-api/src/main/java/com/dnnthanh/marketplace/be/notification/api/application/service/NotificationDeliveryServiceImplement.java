package com.dnnthanh.marketplace.be.notification.api.application.service;

import com.dnnthanh.marketplace.be.notification.api.application.port.in.NotificationDeliveryUseCase;
import com.dnnthanh.marketplace.be.notification.api.application.port.out.NotificationInboxPort;
import com.dnnthanh.marketplace.be.notification.api.application.port.out.NotificationProviderPort;
import com.dnnthanh.marketplace.be.notification.api.domain.model.NotificationDelivery;
import com.dnnthanh.marketplace.be.platform.stereotype.UseCase;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;

/** Worker use case for provider delivery, exponential retry and dead-letter transition. */
@UseCase
@RequiredArgsConstructor
public class NotificationDeliveryServiceImplement implements NotificationDeliveryUseCase {
    private static final int MAX_ATTEMPTS = 5;
    private final NotificationInboxPort inbox;
    private final List<NotificationProviderPort> providers;

    @Override
    public DeliveryBatchResult deliverDue(LocalDateTime now, int requestedLimit) {
        int delivered = 0;
        int retried = 0;
        int deadLettered = 0;
        int limit = Math.max(1, Math.min(requestedLimit, 200));
        for (NotificationInboxPort.DeliveryClaim claim : inbox.claimDue(now, limit)) {
            NotificationDelivery delivery = claim.delivery();
            String attemptKey = claim.attemptKey();
            NotificationProviderPort provider =
                    providers.stream()
                            .filter(candidate -> candidate.supports(delivery.channel()))
                            .findFirst()
                            .orElse(null);
            if (provider == null) {
                delivery.permanentFailure("No provider configured for " + delivery.channel());
                deadLettered++;
            } else {
                NotificationProviderPort.DeliveryResult result;
                try {
                    result = provider.send(delivery, attemptKey);
                } catch (RuntimeException unexpected) {
                    String message =
                            unexpected.getMessage() == null
                                    ? unexpected.getClass().getSimpleName()
                                    : unexpected.getMessage();
                    result = NotificationProviderPort.DeliveryResult.retry(message);
                }
                if (result.success()) {
                    delivery.delivered();
                    delivered++;
                } else if (result.retryable()) {
                    delivery.retryableFailure(result.error(), now, MAX_ATTEMPTS);
                    if (delivery.status() == NotificationDelivery.DeliveryStatus.DEAD_LETTER) {
                        deadLettered++;
                    } else {
                        retried++;
                    }
                } else {
                    delivery.permanentFailure(result.error());
                    deadLettered++;
                }
            }
            inbox.save(delivery);
        }
        return new DeliveryBatchResult(delivered, retried, deadLettered);
    }
}
