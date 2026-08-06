package com.dnnthanh.marketplace.be.media.api.domain.enumtype;

import com.dnnthanh.marketplace.be.platform.model.CodeEnum;

/** Media processing pipeline lifecycle. */
public enum MediaStatus implements CodeEnum {
    INITIATED,
    UPLOADED,
    SCANNING,
    PROCESSING,
    READY,
    FAILED,
    DELETE_PENDING,
    DELETED
}
