package com.dnnthanh.marketplace.be.notification.api.application.query;

import java.time.LocalDateTime;
import java.util.Map;

/** Durable notification read model. */
public record NotificationInboxItem(
        String id,
        String type,
        String title,
        String message,
        Map<String, Object> payload,
        LocalDateTime readAt,
        LocalDateTime createdAt) {}
