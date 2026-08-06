package com.dnnthanh.marketplace.be.comment.api.domain.enumtype;

import com.dnnthanh.marketplace.be.platform.model.CodeEnum;

/** Durable Comment events published through the Mongo outbox. */
public enum CommentEventType implements CodeEnum {
    COMMENT_CREATED,
    COMMENT_REPLIED,
    COMMENT_EDITED,
    COMMENT_DELETED,
    COMMENT_HIDDEN,
    COMMENT_UNHIDDEN,
    COMMENT_REPORTED,
    COMMENT_REACTED,
    COMMENT_REACTION_REMOVED
}
