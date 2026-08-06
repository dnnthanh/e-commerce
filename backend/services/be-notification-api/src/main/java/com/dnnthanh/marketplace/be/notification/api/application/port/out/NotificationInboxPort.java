package com.dnnthanh.marketplace.be.notification.api.application.port.out;

import com.dnnthanh.marketplace.be.notification.api.domain.model.NotificationDelivery;
import com.dnnthanh.marketplace.be.notification.api.domain.model.NotificationPreference;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** Durable notification inbox/preference/delivery boundary backed by MongoDB. */
public interface NotificationInboxPort {
    Optional<NotificationPreference> preference(String userId);

    Optional<NotificationDelivery> findByDeduplicationKey(String key);

    Optional<NotificationDelivery> findDelivery(String notificationId);

    /**
     * Atomically leases due delivery work so multiple worker instances cannot send the same item.
     */
    List<DeliveryClaim> claimDue(LocalDateTime now, int limit);

    NotificationDelivery save(NotificationDelivery delivery);

    /** Provider idempotency key is generated as part of the durable Mongo claim. */
    record DeliveryClaim(NotificationDelivery delivery, String attemptKey) {}
}
