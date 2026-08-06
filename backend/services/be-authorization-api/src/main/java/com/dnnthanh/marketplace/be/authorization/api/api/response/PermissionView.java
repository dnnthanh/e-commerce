package com.dnnthanh.marketplace.be.authorization.api.api.response;

import java.util.Set;

/** Effective roles and permissions resolved from Keycloak outside the JWT. */
public record PermissionView(Set<String> roles, Set<String> permissions) {}
