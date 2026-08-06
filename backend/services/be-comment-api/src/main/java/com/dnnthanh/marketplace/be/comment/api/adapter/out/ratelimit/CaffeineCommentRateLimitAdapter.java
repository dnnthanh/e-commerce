package com.dnnthanh.marketplace.be.comment.api.adapter.out.ratelimit;

import com.dnnthanh.marketplace.be.comment.api.application.exception.CommentRateLimitExceededException;
import com.dnnthanh.marketplace.be.comment.api.application.port.out.CommentRateLimitPort;
import com.dnnthanh.marketplace.be.comment.api.domain.constant.CommentConstants;
import com.dnnthanh.marketplace.be.platform.stereotype.Adapter;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Lightweight per-instance anti-spam limiter; production distributed limits can replace this port.
 */
@Adapter
public class CaffeineCommentRateLimitAdapter implements CommentRateLimitPort {

    private final Cache<String, AtomicInteger> counters =
            Caffeine.newBuilder()
                    .expireAfterWrite(Duration.ofMinutes(1))
                    .maximumSize(100_000)
                    .build();

    @Override
    public void checkAllowed(String userId) {
        AtomicInteger counter = counters.get(userId, ignored -> new AtomicInteger());
        if (counter.incrementAndGet() > CommentConstants.RATE_LIMIT_PER_MINUTE) {
            throw new CommentRateLimitExceededException();
        }
    }
}
