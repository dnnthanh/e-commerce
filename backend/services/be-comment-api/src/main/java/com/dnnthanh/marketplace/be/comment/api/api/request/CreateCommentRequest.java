package com.dnnthanh.marketplace.be.comment.api.api.request;

import com.dnnthanh.marketplace.be.comment.api.domain.constant.CommentConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Request for a new product comment thread. */
public record CreateCommentRequest(
        @NotNull Long productId,
        @NotNull Long sellerId,
        @NotBlank @Size(max = CommentConstants.MAX_CONTENT_LENGTH) String content) {}
