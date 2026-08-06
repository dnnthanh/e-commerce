package com.dnnthanh.marketplace.be.comment.api.api.request;

import com.dnnthanh.marketplace.be.comment.api.domain.constant.CommentConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Request for a reply in an existing thread. */
public record ReplyCommentRequest(
        @NotBlank @Size(max = CommentConstants.MAX_CONTENT_LENGTH) String content) {}
