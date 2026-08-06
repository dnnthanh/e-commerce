package com.dnnthanh.marketplace.be.comment.api.api.response;

import java.time.LocalDateTime;

/** Paged reply response. */
public record CommentReplyResponse(
        String id, String threadId, String authorId, String content, LocalDateTime createdAt) {}
