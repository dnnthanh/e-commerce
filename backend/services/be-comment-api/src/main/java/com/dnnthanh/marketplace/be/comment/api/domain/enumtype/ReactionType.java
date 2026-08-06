package com.dnnthanh.marketplace.be.comment.api.domain.enumtype;

import com.dnnthanh.marketplace.be.platform.i18n.I18nCodeEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

/** Supported idempotent comment reactions. */
@RequiredArgsConstructor
public enum ReactionType implements I18nCodeEnum {
    LIKE("LIKE"),
    HELPFUL("HELPFUL");

    private final String code;

    @Override
    @JsonValue
    public String getCode() {
        return code;
    }
}
