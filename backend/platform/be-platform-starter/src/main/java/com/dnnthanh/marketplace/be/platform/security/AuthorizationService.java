package com.dnnthanh.marketplace.be.platform.security;

import com.dnnthanh.marketplace.be.platform.config.AuthorizationCacheProperties;
import com.dnnthanh.marketplace.be.platform.config.InternalSecurityProperties;
import com.dnnthanh.marketplace.be.platform.context.UserContext;
import com.dnnthanh.marketplace.be.platform.stereotype.Adapter;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import java.util.Set;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.web.client.RestClient;

@Adapter("authorizationService")
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(prefix = "marketplace.internal-security", name = "authorization-base-url")
public class AuthorizationService {

    private final UserContext userContext;
    private final ServiceTokenProvider serviceTokenProvider;
    private final RestClient authorizationClient;
    private final Cache<String, AuthorizationSnapshot> cache;

    public AuthorizationService(
            UserContext userContext,
            ServiceTokenProvider serviceTokenProvider,
            RestClient.Builder restClientBuilder,
            InternalSecurityProperties securityProperties,
            AuthorizationCacheProperties cacheProperties) {
        this.userContext = userContext;
        this.serviceTokenProvider = serviceTokenProvider;
        this.authorizationClient =
                restClientBuilder
                        .clone()
                        .baseUrl(securityProperties.getAuthorizationBaseUrl())
                        .build();
        this.cache =
                Caffeine.newBuilder()
                        .maximumSize(20_000)
                        .expireAfterWrite(
                                Duration.ofSeconds(
                                        Math.max(1, cacheProperties.getCacheTtlSeconds())))
                        .build();
    }

    public boolean hasPermission(String permission) {
        return snapshot().permissions().contains(permission);
    }

    public boolean hasSellerPermission(String permission, Long sellerId) {
        AuthorizationSnapshot snapshot = snapshot();
        return snapshot.permissions().contains(permission)
                && snapshot.sellerIds().contains(sellerId);
    }

    public void evict(String userId) {
        if (userId != null) {
            cache.invalidate(userId);
        }
    }

    public void evictCurrentUser() {
        evict(userContext.userId());
    }

    private AuthorizationSnapshot snapshot() {
        if ("anonymous".equals(userContext.userId())) {
            return AuthorizationSnapshot.empty();
        }
        AuthorizationSnapshot cached = cache.getIfPresent(userContext.userId());
        if (cached != null) {
            return cached;
        }
        try {
            AuthorizationSnapshot value =
                    authorizationClient
                            .get()
                            .uri("/internal/authorization/users/{userId}", userContext.userId())
                            .headers(headers -> headers.setBearerAuth(serviceTokenProvider.token()))
                            .retrieve()
                            .body(AuthorizationSnapshot.class);
            AuthorizationSnapshot resolved = value == null ? AuthorizationSnapshot.empty() : value;
            cache.put(userContext.userId(), resolved);
            return resolved;
        } catch (RuntimeException unavailable) {
            return AuthorizationSnapshot.empty();
        }
    }

    public record AuthorizationSnapshot(
            Set<String> roles, Set<String> permissions, Set<Long> sellerIds) {

        public static AuthorizationSnapshot empty() {
            return new AuthorizationSnapshot(Set.of(), Set.of(), Set.of());
        }
    }
}
