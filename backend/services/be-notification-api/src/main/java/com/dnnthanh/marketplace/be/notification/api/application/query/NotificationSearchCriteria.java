package com.dnnthanh.marketplace.be.notification.api.application.query;

import java.time.LocalDateTime;

public record NotificationSearchCriteria(LocalDateTime after, String afterId, int size) {}
