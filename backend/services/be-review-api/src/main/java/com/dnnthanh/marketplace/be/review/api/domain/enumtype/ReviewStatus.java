package com.dnnthanh.marketplace.be.review.api.domain.enumtype;

import com.dnnthanh.marketplace.be.platform.model.CodeEnum;

/** Review/moderation lifecycle. */
public enum ReviewStatus implements CodeEnum {
    PUBLISHED,
    MODERATION_REQUIRED,
    HIDDEN,
    DELETED
}
