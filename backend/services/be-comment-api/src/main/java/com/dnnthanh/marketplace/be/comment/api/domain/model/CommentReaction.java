package com.dnnthanh.marketplace.be.comment.api.domain.model;

import com.dnnthanh.marketplace.be.comment.api.domain.enumtype.ReactionType;
import java.time.LocalDateTime;

/** Idempotent reaction identity owned by thread/user/type. */
public record CommentReaction(
        String id, String threadId, String userId, ReactionType type, LocalDateTime createdAt) {

    /** Builds a deterministic id so repeated add requests converge to one Mongo document. */
    public static CommentReaction create(
            String threadId, String userId, ReactionType type, LocalDateTime now) {
        return new CommentReaction(
                threadId + ":" + userId + ":" + type.name(), threadId, userId, type, now);
    }
}
