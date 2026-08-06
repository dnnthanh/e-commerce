package com.dnnthanh.marketplace.be.comment.api.api.request;

import com.dnnthanh.marketplace.be.comment.api.domain.enumtype.ReactionType;
import jakarta.validation.constraints.NotNull;

/** Add/remove reaction request. */
public record CommentReactionRequest(@NotNull ReactionType type) {}
