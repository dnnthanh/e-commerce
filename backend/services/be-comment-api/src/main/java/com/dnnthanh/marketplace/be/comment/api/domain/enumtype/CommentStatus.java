package com.dnnthanh.marketplace.be.comment.api.domain.enumtype;

import com.dnnthanh.marketplace.be.platform.i18n.I18nCodeEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

/** User-visible and moderation lifecycle of a comment thread. */
@RequiredArgsConstructor
public enum CommentStatus implements I18nCodeEnum {
    PUBLISHED("PUBLISHED"),
    EDITED("EDITED"),
    DELETED("DELETED"),
    HIDDEN("HIDDEN");

    private final String code;

    @Override
    @JsonValue
    public String getCode() {
        return code;
    }
}
