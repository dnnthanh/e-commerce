package com.dnnthanh.marketplace.be.authorization.api.application.port.in;

import com.dnnthanh.marketplace.be.authorization.api.application.dto.AuthorizationSnapshotDto;
import com.dnnthanh.marketplace.be.authorization.api.application.dto.ManagedShopsDto;
import com.dnnthanh.marketplace.be.authorization.api.application.dto.PermissionViewDto;
import com.dnnthanh.marketplace.be.authorization.api.application.dto.UserAccessContextDto;
import com.dnnthanh.marketplace.be.authorization.api.application.dto.UserProfileDto;

/** Inbound application port for Keycloak-backed authorization queries and durable mutations. */
public interface AuthorizationUseCase {
    AuthorizationSnapshotDto current();

    AuthorizationSnapshotDto snapshot(String userId);

    UserProfileDto currentProfile();

    UserProfileDto profile(String userId);

    PermissionViewDto currentPermissions();

    PermissionViewDto permissions(String userId);

    ManagedShopsDto currentManagedShops();

    ManagedShopsDto managedShops(String userId);

    UserAccessContextDto currentContext();

    UserAccessContextDto context(String userId);

    void assignRole(String userId, String role);

    void removeRole(String userId, String role);

    void assignSeller(String userId, Long sellerId);

    void removeSeller(String userId, Long sellerId);

    void assignShop(String userId, Long sellerId, Long shopId);

    void removeShop(String userId, Long sellerId, Long shopId);

    ReconciliationResult reconcilePending();

    record ReconciliationResult(int applied, int failed) {}
}
