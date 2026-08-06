package com.dnnthanh.marketplace.be.review.api.api.response;

import java.time.LocalDateTime;

public record ReviewView(
        Long id,
        Long productId,
        int rating,
        String title,
        String content,
        LocalDateTime createdAt) {}
