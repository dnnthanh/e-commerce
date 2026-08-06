package com.dnnthanh.marketplace.be.notification.api.adapter.out.mongo;

import com.dnnthanh.marketplace.be.notification.api.domain.enumtype.NotificationChannel;
import com.dnnthanh.marketplace.be.notification.api.domain.model.NotificationDelivery.DeliveryStatus;
import java.time.LocalDateTime;
import java.util.Set;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/** Mongo representation of provider delivery state. */
@Document("notification_delivery")
public record NotificationDeliveryDocument(
        @Id String notificationId,
        @Indexed(unique = true) String deduplicationKey,
        @Indexed String userId,
        NotificationChannel channel,
        Set<String> providerAttemptKeys,
        int attempts,
        @Indexed DeliveryStatus status,
        @Indexed LocalDateTime nextAttemptAt,
        String lastError) {}
