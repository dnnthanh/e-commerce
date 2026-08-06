package com.dnnthanh.marketplace.be.authorization.api.application.service;

import com.dnnthanh.marketplace.be.authorization.api.application.AuthorizationChangeRecorder;
import com.dnnthanh.marketplace.be.authorization.api.application.dto.AuthorizationSnapshotDto;
import com.dnnthanh.marketplace.be.authorization.api.application.dto.ManagedShopsDto;
import com.dnnthanh.marketplace.be.authorization.api.application.dto.PermissionViewDto;
import com.dnnthanh.marketplace.be.authorization.api.application.dto.UserAccessContextDto;
import com.dnnthanh.marketplace.be.authorization.api.application.dto.UserProfileDto;
import com.dnnthanh.marketplace.be.authorization.api.application.port.in.AuthorizationUseCase;
import com.dnnthanh.marketplace.be.authorization.api.application.port.in.AuthorizationUseCase.ReconciliationResult;
import com.dnnthanh.marketplace.be.authorization.api.application.port.out.AuthorizationChangePort.PendingChange;
import com.dnnthanh.marketplace.be.authorization.api.application.port.out.AuthorizationProviderPort;
import com.dnnthanh.marketplace.be.authorization.api.domain.model.AuthorizationChangeType;
import com.dnnthanh.marketplace.be.platform.cache.MarketplaceCacheNames;
import com.dnnthanh.marketplace.be.platform.context.UserContext;
import com.dnnthanh.marketplace.be.platform.stereotype.UseCase;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

/** Keycloak-backed authorization queries plus durable intent-before-provider mutations. */
@UseCase
@RequiredArgsConstructor
public class AuthorizationServiceImplement implements AuthorizationUseCase {
    private static final int RECONCILIATION_BATCH_SIZE = 100;
    private final AuthorizationProviderPort provider;
    private final AuthorizationChangeRecorder recorder;
    private final UserContext userContext;

    @Override
    public AuthorizationSnapshotDto current() {
        return currentContext().authorizationSnapshot();
    }

    @Override
    public AuthorizationSnapshotDto snapshot(String userId) {
        return context(userId).authorizationSnapshot();
    }

    @Override
    public UserProfileDto currentProfile() {
        return currentContext().profile();
    }

    @Override
    public UserProfileDto profile(String userId) {
        return context(userId).profile();
    }

    @Override
    public PermissionViewDto currentPermissions() {
        return currentContext().permissionView();
    }

    @Override
    public PermissionViewDto permissions(String userId) {
        return context(userId).permissionView();
    }

    @Override
    public ManagedShopsDto currentManagedShops() {
        return currentContext().managedShops();
    }

    @Override
    public ManagedShopsDto managedShops(String userId) {
        return context(userId).managedShops();
    }

    @Override
    public UserAccessContextDto currentContext() {
        return context(userContext.userId());
    }

    @Cacheable(
            cacheNames = MarketplaceCacheNames.AUTHORIZATION_SNAPSHOTS,
            key = "#userId",
            unless = "#result == null",
            sync = true)
    @Override
    public UserAccessContextDto context(String userId) {
        return provider.context(userId);
    }

    @Override
    @CacheEvict(cacheNames = MarketplaceCacheNames.AUTHORIZATION_SNAPSHOTS, key = "#userId")
    public void assignRole(String userId, String role) {
        executePrepared(
                userId,
                AuthorizationChangeType.ROLE_ASSIGNED,
                currentActorId(),
                Map.of("role", role));
    }

    @Override
    @CacheEvict(cacheNames = MarketplaceCacheNames.AUTHORIZATION_SNAPSHOTS, key = "#userId")
    public void removeRole(String userId, String role) {
        executePrepared(
                userId,
                AuthorizationChangeType.ROLE_REMOVED,
                currentActorId(),
                Map.of("role", role));
    }

    @Override
    @CacheEvict(cacheNames = MarketplaceCacheNames.AUTHORIZATION_SNAPSHOTS, key = "#userId")
    public void assignSeller(String userId, Long sellerId) {
        executePrepared(
                userId,
                AuthorizationChangeType.SELLER_SCOPE_ASSIGNED,
                currentActorId(),
                Map.of("sellerId", sellerId));
    }

    @Override
    @CacheEvict(cacheNames = MarketplaceCacheNames.AUTHORIZATION_SNAPSHOTS, key = "#userId")
    public void removeSeller(String userId, Long sellerId) {
        executePrepared(
                userId,
                AuthorizationChangeType.SELLER_SCOPE_REMOVED,
                currentActorId(),
                Map.of("sellerId", sellerId));
    }

    @Override
    @CacheEvict(cacheNames = MarketplaceCacheNames.AUTHORIZATION_SNAPSHOTS, key = "#userId")
    public void assignShop(String userId, Long sellerId, Long shopId) {
        executePrepared(
                userId,
                AuthorizationChangeType.SHOP_SCOPE_ASSIGNED,
                currentActorId(),
                Map.of("sellerId", sellerId, "shopId", shopId));
    }

    @Override
    @CacheEvict(cacheNames = MarketplaceCacheNames.AUTHORIZATION_SNAPSHOTS, key = "#userId")
    public void removeShop(String userId, Long sellerId, Long shopId) {
        executePrepared(
                userId,
                AuthorizationChangeType.SHOP_SCOPE_REMOVED,
                currentActorId(),
                Map.of("sellerId", sellerId, "shopId", shopId));
    }

    private void executePrepared(
            String userId,
            AuthorizationChangeType type,
            String actor,
            Map<String, Object> details) {
        String changeId = recorder.prepareChange(userId, type, actor, details);
        try {
            applyProvider(userId, type, details);
            recorder.markApplied(changeId);
        } catch (RuntimeException failure) {
            recorder.markFailed(changeId, failure);
            throw failure;
        }
    }

    @Override
    public ReconciliationResult reconcilePending() {
        int applied = 0;
        int failed = 0;
        for (PendingChange change : recorder.pending(RECONCILIATION_BATCH_SIZE)) {
            try {
                applyProvider(change.userId(), change.changeType(), change.details());
                recorder.markApplied(change.changeId());
                applied++;
            } catch (RuntimeException failure) {
                recorder.markFailed(change.changeId(), failure);
                failed++;
            }
        }
        return new ReconciliationResult(applied, failed);
    }

    private void applyProvider(
            String userId, AuthorizationChangeType type, Map<String, Object> details) {
        switch (type) {
            case ROLE_ASSIGNED -> provider.assignRole(userId, String.valueOf(details.get("role")));
            case ROLE_REMOVED -> provider.removeRole(userId, String.valueOf(details.get("role")));
            case SELLER_SCOPE_ASSIGNED ->
                    provider.assignSeller(userId, asLong(details.get("sellerId")));
            case SELLER_SCOPE_REMOVED ->
                    provider.removeSeller(userId, asLong(details.get("sellerId")));
            case SHOP_SCOPE_ASSIGNED ->
                    provider.assignShop(
                            userId, asLong(details.get("sellerId")), asLong(details.get("shopId")));
            case SHOP_SCOPE_REMOVED ->
                    provider.removeShop(
                            userId, asLong(details.get("sellerId")), asLong(details.get("shopId")));
        }
    }

    private String currentActorId() {
        return userContext.userId();
    }

    private static Long asLong(Object value) {
        return value instanceof Number number
                ? number.longValue()
                : Long.valueOf(String.valueOf(value));
    }
}
