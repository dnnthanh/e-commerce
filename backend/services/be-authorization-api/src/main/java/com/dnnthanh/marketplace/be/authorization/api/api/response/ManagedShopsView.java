package com.dnnthanh.marketplace.be.authorization.api.api.response;

import java.util.List;

/** Seller/shop scopes currently managed by a Keycloak user. */
public record ManagedShopsView(List<ManagedSellerScopeView> sellers) {}
