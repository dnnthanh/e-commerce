package com.dnnthanh.marketplace.be.authorization.api.config;

import com.dnnthanh.marketplace.be.authorization.api.application.dto.UserAccessContextDto;
import com.dnnthanh.marketplace.be.platform.cache.MarketplaceCacheNames;
import com.dnnthanh.marketplace.be.platform.cache.RedisCacheSpec;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Contributes the authorization context cache to the shared Redis cache manager. */
@Configuration(proxyBeanMethods = false)
public class AuthorizationCacheConfiguration {

    /**
     * Defines a short TTL because authorization data is security-sensitive and also explicitly
     * invalidated.
     */
    @Bean
    public RedisCacheSpec authorizationContextCacheSpec() {
        return new RedisCacheSpec(
                MarketplaceCacheNames.AUTHORIZATION_SNAPSHOTS,
                Duration.ofMinutes(2),
                UserAccessContextDto.class);
    }
}
