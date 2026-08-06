package com.dnnthanh.marketplace.be.notification.api.application.port.out;

import com.dnnthanh.marketplace.be.notification.api.domain.enumtype.NotificationChannel;
import com.dnnthanh.marketplace.be.notification.api.domain.model.NotificationDelivery;

/** External notification provider boundary. */
public interface NotificationProviderPort {
    boolean supports(NotificationChannel channel);

    DeliveryResult send(NotificationDelivery delivery, String providerAttemptKey);

    record DeliveryResult(boolean success, boolean retryable, String error) {
        public static DeliveryResult delivered() {
            return new DeliveryResult(true, false, null);
        }

        public static DeliveryResult retry(String error) {
            return new DeliveryResult(false, true, error);
        }

        public static DeliveryResult permanentFailure(String error) {
            return new DeliveryResult(false, false, error);
        }
    }
}
