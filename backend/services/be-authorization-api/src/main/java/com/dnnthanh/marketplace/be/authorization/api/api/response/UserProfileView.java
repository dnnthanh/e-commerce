package com.dnnthanh.marketplace.be.authorization.api.api.response;

import java.util.List;
import java.util.Map;

/** Keycloak-owned user profile. */
public record UserProfileView(
        String userId,
        String username,
        String email,
        String firstName,
        String lastName,
        boolean enabled,
        boolean emailVerified,
        Map<String, List<String>> attributes) {}
