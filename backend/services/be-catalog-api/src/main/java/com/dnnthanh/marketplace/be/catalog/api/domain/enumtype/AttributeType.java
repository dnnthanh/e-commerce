package com.dnnthanh.marketplace.be.catalog.api.domain.enumtype;

import com.dnnthanh.marketplace.be.platform.model.CodeEnum;

/** Supported dynamic catalog attribute types. */
public enum AttributeType implements CodeEnum {
    TEXT,
    NUMBER,
    BOOLEAN,
    SINGLE_SELECT,
    MULTI_SELECT
}
