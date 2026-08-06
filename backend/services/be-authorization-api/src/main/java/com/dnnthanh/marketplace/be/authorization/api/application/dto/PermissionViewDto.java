package com.dnnthanh.marketplace.be.authorization.api.application.dto;

import java.util.Set;

/** Effective Keycloak role/permission composition returned outside the JWT. */
public record PermissionViewDto(Set<String> roles, Set<String> permissions) {
    public PermissionViewDto {
        roles = roles == null ? Set.of() : Set.copyOf(roles);
        permissions = permissions == null ? Set.of() : Set.copyOf(permissions);
    }
}
