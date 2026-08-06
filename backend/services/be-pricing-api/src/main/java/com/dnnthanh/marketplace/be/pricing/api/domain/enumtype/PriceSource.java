package com.dnnthanh.marketplace.be.pricing.api.domain.enumtype;

import com.dnnthanh.marketplace.be.platform.model.CodeEnum;

/** Origin of an effective price. */
public enum PriceSource implements CodeEnum {
    BASE,
    SELLER,
    CHANNEL,
    SCHEDULED
}
