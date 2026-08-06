package com.dnnthanh.marketplace.be.authorization.api.domain.model;

import java.util.Objects;
import java.util.Set;

/** Effective permission snapshot with explicit seller-resource scope checks. */
public record AuthorizationPolicy(
        String userId, Set<String> permissions, Set<Long> sellerIds, long policyVersion) {
    public AuthorizationPolicy {
        Objects.requireNonNull(userId);
        permissions = permissions == null ? Set.of() : Set.copyOf(permissions);
        sellerIds = sellerIds == null ? Set.of() : Set.copyOf(sellerIds);
        if (policyVersion < 0)
            throw new InvalidAuthorizationPolicyException("Policy version cannot be negative");
    }

    public boolean allows(String permission) {
        return permissions.contains(permission);
    }

    public boolean allowsSeller(String permission, Long sellerId) {
        return permissions.contains(permission) && sellerIds.contains(sellerId);
    }

    /** Fails closed when a cached snapshot is older than the required mutation version. */
    public boolean isFreshEnough(long requiredVersion) {
        return policyVersion >= requiredVersion;
    }

    /** Invalid authorization-policy data from the identity/policy provider. */
    public static final class InvalidAuthorizationPolicyException extends RuntimeException {
        public InvalidAuthorizationPolicyException(String message) {
            super(message);
        }
    }
}
