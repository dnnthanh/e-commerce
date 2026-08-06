package com.dnnthanh.marketplace.be.authorization.api.application.dto;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

/** Full Keycloak-backed identity and authorization context. */
public record UserAccessContextDto(
        UserProfileDto profile,
        Set<String> roles,
        Set<String> permissions,
        List<ManagedSellerScopeDto> sellerScopes,
        Set<String> groupPaths) {
    public UserAccessContextDto {
        roles = roles == null ? Set.of() : Set.copyOf(roles);
        permissions = permissions == null ? Set.of() : Set.copyOf(permissions);
        sellerScopes =
                sellerScopes == null
                        ? List.of()
                        : sellerScopes.stream()
                                .sorted(Comparator.comparing(ManagedSellerScopeDto::sellerId))
                                .toList();
        groupPaths = groupPaths == null ? Set.of() : Set.copyOf(groupPaths);
    }

    public AuthorizationSnapshotDto authorizationSnapshot() {
        Set<Long> sellerIds =
                sellerScopes.stream()
                        .map(ManagedSellerScopeDto::sellerId)
                        .collect(java.util.stream.Collectors.toUnmodifiableSet());
        return new AuthorizationSnapshotDto(roles, permissions, sellerIds);
    }

    public PermissionViewDto permissionView() {
        return new PermissionViewDto(roles, permissions);
    }

    public ManagedShopsDto managedShops() {
        return new ManagedShopsDto(sellerScopes);
    }
}
