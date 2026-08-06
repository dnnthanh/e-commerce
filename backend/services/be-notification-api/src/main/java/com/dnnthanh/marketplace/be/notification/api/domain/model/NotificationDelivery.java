package com.dnnthanh.marketplace.be.notification.api.domain.model;

import com.dnnthanh.marketplace.be.notification.api.domain.enumtype.NotificationChannel;
import com.dnnthanh.marketplace.be.platform.model.CodeEnum;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Durable provider delivery aggregate with deduplication, retry/backoff and dead-letter lifecycle.
 */
public final class NotificationDelivery {
    private final String notificationId;
    private final String deduplicationKey;
    private final String userId;
    private final NotificationChannel channel;
    private final Set<String> providerAttemptKeys;
    private int attempts;
    private DeliveryStatus status;
    private LocalDateTime nextAttemptAt;
    private String lastError;

    public NotificationDelivery(
            String notificationId,
            String deduplicationKey,
            String userId,
            NotificationChannel channel) {
        this(
                notificationId,
                deduplicationKey,
                userId,
                channel,
                Set.of(),
                0,
                DeliveryStatus.PENDING,
                LocalDateTime.now(),
                null);
    }

    private NotificationDelivery(
            String notificationId,
            String deduplicationKey,
            String userId,
            NotificationChannel channel,
            Set<String> providerAttemptKeys,
            int attempts,
            DeliveryStatus status,
            LocalDateTime nextAttemptAt,
            String lastError) {
        this.notificationId = Objects.requireNonNull(notificationId);
        this.deduplicationKey = Objects.requireNonNull(deduplicationKey);
        this.userId = Objects.requireNonNull(userId);
        this.channel = Objects.requireNonNull(channel);
        this.providerAttemptKeys = new HashSet<>(providerAttemptKeys);
        this.attempts = attempts;
        this.status = Objects.requireNonNull(status);
        this.nextAttemptAt = nextAttemptAt;
        this.lastError = lastError;
    }

    public static NotificationDelivery rehydrate(
            String notificationId,
            String deduplicationKey,
            String userId,
            NotificationChannel channel,
            Set<String> providerAttemptKeys,
            int attempts,
            DeliveryStatus status,
            LocalDateTime nextAttemptAt,
            String lastError) {
        return new NotificationDelivery(
                notificationId,
                deduplicationKey,
                userId,
                channel,
                providerAttemptKeys,
                attempts,
                status,
                nextAttemptAt,
                lastError);
    }

    public boolean startAttempt(String providerAttemptKey) {
        if (status == DeliveryStatus.DELIVERED
                || status == DeliveryStatus.DEAD_LETTER
                || !providerAttemptKeys.add(providerAttemptKey)) {
            return false;
        }
        attempts++;
        status = DeliveryStatus.SENDING;
        lastError = null;
        return true;
    }

    public void delivered() {
        status = DeliveryStatus.DELIVERED;
        nextAttemptAt = null;
        lastError = null;
    }

    public void retryableFailure(String error, LocalDateTime now, int maxAttempts) {
        lastError = error;
        if (attempts >= maxAttempts) {
            status = DeliveryStatus.DEAD_LETTER;
            nextAttemptAt = null;
            return;
        }
        status = DeliveryStatus.FAILED_RETRYABLE;
        long delaySeconds = Math.min(3600, 30L * (1L << Math.min(attempts - 1, 7)));
        nextAttemptAt = now.plus(Duration.ofSeconds(delaySeconds));
    }

    public void permanentFailure(String error) {
        lastError = error;
        status = DeliveryStatus.DEAD_LETTER;
        nextAttemptAt = null;
    }

    public void replay(LocalDateTime now) {
        if (status != DeliveryStatus.DEAD_LETTER) {
            return;
        }
        status = DeliveryStatus.PENDING;
        nextAttemptAt = now;
        lastError = null;
    }

    public boolean due(LocalDateTime now) {
        return (status == DeliveryStatus.PENDING || status == DeliveryStatus.FAILED_RETRYABLE)
                && (nextAttemptAt == null || !nextAttemptAt.isAfter(now));
    }

    public String notificationId() {
        return notificationId;
    }

    public String deduplicationKey() {
        return deduplicationKey;
    }

    public String userId() {
        return userId;
    }

    public NotificationChannel channel() {
        return channel;
    }

    public Set<String> providerAttemptKeys() {
        return Set.copyOf(providerAttemptKeys);
    }

    public int attempts() {
        return attempts;
    }

    public DeliveryStatus status() {
        return status;
    }

    public LocalDateTime nextAttemptAt() {
        return nextAttemptAt;
    }

    public String lastError() {
        return lastError;
    }

    public enum DeliveryStatus implements CodeEnum {
        PENDING,
        SENDING,
        FAILED_RETRYABLE,
        DELIVERED,
        DEAD_LETTER
    }
}
