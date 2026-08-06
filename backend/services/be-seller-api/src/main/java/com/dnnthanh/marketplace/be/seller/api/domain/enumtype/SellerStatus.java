package com.dnnthanh.marketplace.be.seller.api.domain.enumtype;

import com.dnnthanh.marketplace.be.platform.model.CodeEnum;

/** Seller onboarding/operations lifecycle. */
public enum SellerStatus implements CodeEnum {
    DRAFT,
    SUBMITTED,
    VERIFIED,
    ACTIVE,
    SUSPENDED,
    CLOSED
}
