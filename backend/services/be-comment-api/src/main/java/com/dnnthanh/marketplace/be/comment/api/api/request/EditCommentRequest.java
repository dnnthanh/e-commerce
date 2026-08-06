package com.dnnthanh.marketplace.be.comment.api.api.request;

import com.dnnthanh.marketplace.be.comment.api.domain.constant.CommentConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Owner edit request for a comment thread. */
public record EditCommentRequest(
        @NotBlank @Size(max = CommentConstants.MAX_CONTENT_LENGTH) String content) {}
