package com.dnnthanh.marketplace.be.promotion.api.domain.enumtype;

import com.dnnthanh.marketplace.be.platform.model.CodeEnum;

/** Usage-reservation lifecycle protecting promotion redemption limits. */
public enum PromotionReservationStatus implements CodeEnum {
    RESERVED,
    CONFIRMED,
    RELEASED
}
