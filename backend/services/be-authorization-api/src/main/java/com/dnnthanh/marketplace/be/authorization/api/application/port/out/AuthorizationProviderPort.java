package com.dnnthanh.marketplace.be.authorization.api.application.port.out;

import com.dnnthanh.marketplace.be.authorization.api.application.dto.UserAccessContextDto;

/** Outbound port for Keycloak-owned identity and authorization administration. */
public interface AuthorizationProviderPort {
    UserAccessContextDto context(String userId);

    void assignRole(String userId, String role);

    void removeRole(String userId, String role);

    void assignSeller(String userId, Long sellerId);

    void removeSeller(String userId, Long sellerId);

    void assignShop(String userId, Long sellerId, Long shopId);

    void removeShop(String userId, Long sellerId, Long shopId);
}
