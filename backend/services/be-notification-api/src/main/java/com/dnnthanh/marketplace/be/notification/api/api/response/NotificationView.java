package com.dnnthanh.marketplace.be.notification.api.api.response;

import java.time.LocalDateTime;
import java.util.Map;

public record NotificationView(
        String id,
        String type,
        String title,
        String message,
        Map<String, Object> payload,
        LocalDateTime readAt,
        LocalDateTime createdAt) {}
