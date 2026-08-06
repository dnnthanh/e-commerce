package com.dnnthanh.marketplace.be.promotion.api.domain.enumtype;

import com.dnnthanh.marketplace.be.platform.model.CodeEnum;

/** Supported promotion benefit calculation styles. */
public enum PromotionBenefitType implements CodeEnum {
    FIXED_AMOUNT,
    PERCENTAGE,
    SHIPPING_DISCOUNT
}
