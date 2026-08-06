package com.dnnthanh.marketplace.be.notification.api.adapter.out.mongo;

import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("notification")
public record NotificationDocument(
        @Id String id,
        String userId,
        String type,
        String title,
        String message,
        Map<String, Object> payload,
        LocalDateTime readAt,
        LocalDateTime createdAt) {}
