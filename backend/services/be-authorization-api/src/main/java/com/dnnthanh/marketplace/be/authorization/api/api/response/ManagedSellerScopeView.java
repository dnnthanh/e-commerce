package com.dnnthanh.marketplace.be.authorization.api.api.response;

import com.dnnthanh.marketplace.be.platform.model.CodeEnum;
import java.util.Set;

/** Managed seller and shop scope. */
public record ManagedSellerScopeView(Long sellerId, Access access, Set<Long> shopIds) {
    public enum Access implements CodeEnum {
        ALL_SHOPS,
        SELECTED_SHOPS
    }
}
