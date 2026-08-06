package com.dnnthanh.marketplace.be.authorization.api.application.dto;

import java.util.List;
import java.util.Map;

/** Keycloak-owned identity profile safe for authenticated application use. */
public record UserProfileDto(
        String userId,
        String username,
        String email,
        String firstName,
        String lastName,
        boolean enabled,
        boolean emailVerified,
        Map<String, List<String>> attributes) {
    public UserProfileDto {
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }
}
