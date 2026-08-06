package com.dnnthanh.marketplace.be.authorization.api.domain.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.Test;

class AuthorizationPolicyTest {
    @Test
    void enforcesPermissionAndSellerScopeAndVersionFreshness() {
        AuthorizationPolicy p = new AuthorizationPolicy("U", Set.of("ORDER_VIEW"), Set.of(10L), 3);
        assertTrue(p.allowsSeller("ORDER_VIEW", 10L));
        assertFalse(p.allowsSeller("ORDER_VIEW", 11L));
        assertFalse(p.isFreshEnough(4));
    }
}
