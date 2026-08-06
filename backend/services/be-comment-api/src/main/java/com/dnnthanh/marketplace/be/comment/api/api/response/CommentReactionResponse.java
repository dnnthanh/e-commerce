package com.dnnthanh.marketplace.be.comment.api.api.response;

import com.dnnthanh.marketplace.be.comment.api.domain.enumtype.ReactionType;
import java.time.LocalDateTime;

/** Reaction mutation response. */
public record CommentReactionResponse(
        String id, String threadId, String userId, ReactionType type, LocalDateTime createdAt) {}
