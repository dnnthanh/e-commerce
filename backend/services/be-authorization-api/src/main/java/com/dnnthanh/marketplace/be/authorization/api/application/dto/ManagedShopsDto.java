package com.dnnthanh.marketplace.be.authorization.api.application.dto;

import java.util.List;

/** Managed seller/shop scopes for the current or requested Keycloak user. */
public record ManagedShopsDto(List<ManagedSellerScopeDto> sellers) {
    public ManagedShopsDto {
        sellers = sellers == null ? List.of() : List.copyOf(sellers);
    }
}
