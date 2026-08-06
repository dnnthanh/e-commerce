package com.dnnthanh.marketplace.be.authorization.api.application.dto;

import java.util.Set;

/** Effective authorization state independent of HTTP and Keycloak representations. */
public record AuthorizationSnapshotDto(
        Set<String> roles, Set<String> permissions, Set<Long> sellerIds) {
    public AuthorizationSnapshotDto {
        roles = roles == null ? Set.of() : Set.copyOf(roles);
        permissions = permissions == null ? Set.of() : Set.copyOf(permissions);
        sellerIds = sellerIds == null ? Set.of() : Set.copyOf(sellerIds);
    }
}
