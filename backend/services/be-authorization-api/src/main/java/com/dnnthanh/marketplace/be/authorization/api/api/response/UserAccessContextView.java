package com.dnnthanh.marketplace.be.authorization.api.api.response;

import java.util.List;
import java.util.Set;

/** Full Keycloak-backed identity and authorization context. */
public record UserAccessContextView(
        UserProfileView profile,
        Set<String> roles,
        Set<String> permissions,
        List<ManagedSellerScopeView> managedShops,
        Set<String> groupPaths) {}
