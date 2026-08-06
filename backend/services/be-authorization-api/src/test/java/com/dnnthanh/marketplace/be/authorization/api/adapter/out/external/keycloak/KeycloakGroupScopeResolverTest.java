package com.dnnthanh.marketplace.be.authorization.api.adapter.out.external.keycloak;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.Test;

class KeycloakGroupScopeResolverTest {

    @Test
    void sellerParentMembershipGrantsAllShops() {
        var scopes = KeycloakGroupScopeResolver.resolve(Set.of("/sellers/seller-10001"));
        var scope = scopes.get(10001L);
        assertTrue(scope.allShops());
        assertTrue(scope.shopIds().isEmpty());
    }

    @Test
    void shopLeafMembershipsAreSellerScoped() {
        var scopes =
                KeycloakGroupScopeResolver.resolve(
                        Set.of(
                                "/sellers/seller-10001/shops/shop-11001",
                                "/sellers/seller-10001/shops/shop-11009",
                                "/sellers/seller-10002/shops/shop-11002"));
        assertFalse(scopes.get(10001L).allShops());
        assertEquals(Set.of(11001L, 11009L), scopes.get(10001L).shopIds());
        assertEquals(Set.of(11002L), scopes.get(10002L).shopIds());
    }

    @Test
    void sellerParentOverridesSelectedLeaves() {
        var scopes =
                KeycloakGroupScopeResolver.resolve(
                        Set.of("/sellers/seller-10001/shops/shop-11001", "/sellers/seller-10001"));
        assertTrue(scopes.get(10001L).allShops());
        assertTrue(scopes.get(10001L).shopIds().isEmpty());
    }
}
