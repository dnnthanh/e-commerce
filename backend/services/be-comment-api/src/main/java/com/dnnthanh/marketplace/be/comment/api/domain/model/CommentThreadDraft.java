package com.dnnthanh.marketplace.be.comment.api.domain.model;

import lombok.Builder;

/** Validated creation input for a new comment thread. */
@Builder
public record CommentThreadDraft(Long productId, Long sellerId, String authorId, String content) {}
