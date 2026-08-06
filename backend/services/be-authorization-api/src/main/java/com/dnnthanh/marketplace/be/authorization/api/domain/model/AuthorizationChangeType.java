package com.dnnthanh.marketplace.be.authorization.api.domain.model;

import com.dnnthanh.marketplace.be.platform.model.CodeEnum;

/** Closed set of privileged authorization mutations. */
public enum AuthorizationChangeType implements CodeEnum {
    ROLE_ASSIGNED,
    ROLE_REMOVED,
    SELLER_SCOPE_ASSIGNED,
    SELLER_SCOPE_REMOVED,
    SHOP_SCOPE_ASSIGNED,
    SHOP_SCOPE_REMOVED
}
