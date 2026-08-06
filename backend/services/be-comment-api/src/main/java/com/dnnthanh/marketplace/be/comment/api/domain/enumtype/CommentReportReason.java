package com.dnnthanh.marketplace.be.comment.api.domain.enumtype;

import com.dnnthanh.marketplace.be.platform.i18n.I18nCodeEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

/** Closed set of report reasons used by moderation workflows. */
@RequiredArgsConstructor
public enum CommentReportReason implements I18nCodeEnum {
    SPAM("SPAM"),
    HARASSMENT("HARASSMENT"),
    OFF_TOPIC("OFF_TOPIC"),
    MISLEADING("MISLEADING"),
    OTHER("OTHER");

    private final String code;

    @Override
    @JsonValue
    public String getCode() {
        return code;
    }
}
