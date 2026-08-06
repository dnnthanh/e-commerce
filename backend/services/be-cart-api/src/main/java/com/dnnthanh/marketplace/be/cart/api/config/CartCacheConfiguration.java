package com.dnnthanh.marketplace.be.cart.api.config;

import com.dnnthanh.marketplace.be.cart.api.adapter.out.cache.CartCacheDocument;
import com.dnnthanh.marketplace.be.platform.cache.MarketplaceCacheNames;
import com.dnnthanh.marketplace.be.platform.cache.RedisCacheSpec;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Typed cache configuration; short TTL limits stale cart snapshots across devices. */
@Configuration(proxyBeanMethods = false)
public class CartCacheConfiguration {
    @Bean
    RedisCacheSpec cartCacheSpec() {
        return new RedisCacheSpec(
                MarketplaceCacheNames.CARTS, Duration.ofMinutes(15), CartCacheDocument.class);
    }
}
