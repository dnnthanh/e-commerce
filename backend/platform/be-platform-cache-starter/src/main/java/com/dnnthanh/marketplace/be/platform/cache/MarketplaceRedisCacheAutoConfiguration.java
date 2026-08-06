package com.dnnthanh.marketplace.be.platform.cache;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.cache.autoconfigure.CacheAutoConfiguration;
import org.springframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.ObjectMapper;

/**
 * Enables Spring Cache and builds one transaction-aware Redis cache manager from typed cache specs.
 * Business code only reads/writes domain-facing objects; serialization stays in infrastructure.
 */
@AutoConfiguration
@AutoConfigureAfter(DataRedisAutoConfiguration.class)
@AutoConfigureBefore(CacheAutoConfiguration.class)
@EnableCaching
@ConditionalOnClass(RedisCacheManager.class)
@ConditionalOnBean(RedisConnectionFactory.class)
public class MarketplaceRedisCacheAutoConfiguration implements CachingConfigurer {

    private final ObjectMapper objectMapper;

    public MarketplaceRedisCacheAutoConfiguration(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Builds a Redis-backed cache manager with an explicit serializer and TTL for every cache.
     * Transaction-aware mode delays cache mutations until the surrounding DB transaction commits.
     *
     * @param connectionFactory Redis connection factory managed by Spring Boot
     * @param specs cache definitions contributed by individual bounded contexts
     * @return configured cache manager
     */
    @Bean
    @ConditionalOnMissingBean(CacheManager.class)
    public RedisCacheManager marketplaceRedisCacheManager(
            RedisConnectionFactory connectionFactory, List<RedisCacheSpec> specs) {
        Map<String, RedisCacheConfiguration> configurations = new LinkedHashMap<>();
        for (RedisCacheSpec spec : specs) {
            configurations.put(spec.name(), cacheConfiguration(spec));
        }

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfiguration())
                .withInitialCacheConfigurations(configurations)
                .allowCreateOnMissingCache(false)
                .enableStatistics()
                .transactionAware()
                .build();
    }

    /**
     * Exposes one generic typed cache facade for imperative cache-aside adapters.
     *
     * @param cacheManager centrally configured Spring cache manager
     * @return generic marketplace cache facade
     */
    @Bean
    @ConditionalOnMissingBean
    public MarketplaceCacheManager marketplaceCacheManager(CacheManager cacheManager) {
        return new MarketplaceCacheManager(cacheManager);
    }

    /**
     * Uses fail-open semantics because Redis is never authoritative business storage in this
     * project.
     */
    @Override
    public CacheErrorHandler errorHandler() {
        return new FailOpenCacheErrorHandler();
    }

    private RedisCacheConfiguration defaultConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues()
                .computePrefixWith(name -> "marketplace::" + name + "::")
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new StringRedisSerializer()));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private RedisCacheConfiguration cacheConfiguration(RedisCacheSpec spec) {
        RedisSerializer serializer = new JacksonJsonRedisSerializer(objectMapper, spec.valueType());
        return defaultConfiguration()
                .entryTtl(spec.ttl())
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(serializer));
    }
}
