package com.dnnthanh.marketplace.be.comment.api.application.command;

import com.dnnthanh.marketplace.be.comment.api.domain.enumtype.ReactionType;

/** Application command for adding or removing a comment reaction. */
public record CommentReactionCommand(String threadId, ReactionType type) {}
