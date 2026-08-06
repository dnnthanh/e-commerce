package com.dnnthanh.marketplace.be.comment.api.domain.constant;

import lombok.experimental.UtilityClass;

/** Technical limits shared by comment adapters/use cases. */
@UtilityClass
public class CommentConstants {
    public static final int MAX_CONTENT_LENGTH = 5_000;
    public static final int RATE_LIMIT_PER_MINUTE = 10;
    public static final String MENTION_PATTERN = "(?<![\\w@])@([A-Za-z0-9._-]{2,64})";
    public static final String OUTBOX_PENDING_STATUS = "PENDING";
}
