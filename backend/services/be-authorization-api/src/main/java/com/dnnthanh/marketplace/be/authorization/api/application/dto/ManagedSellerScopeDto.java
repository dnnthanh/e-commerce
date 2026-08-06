package com.dnnthanh.marketplace.be.authorization.api.application.dto;

import com.dnnthanh.marketplace.be.platform.model.CodeEnum;
import java.util.Set;

/** Seller/shop authorization scope derived only from Keycloak group membership. */
public record ManagedSellerScopeDto(Long sellerId, Access access, Set<Long> shopIds) {
    public ManagedSellerScopeDto {
        shopIds = shopIds == null ? Set.of() : Set.copyOf(shopIds);
    }

    public enum Access implements CodeEnum {
        ALL_SHOPS,
        SELECTED_SHOPS
    }
}
