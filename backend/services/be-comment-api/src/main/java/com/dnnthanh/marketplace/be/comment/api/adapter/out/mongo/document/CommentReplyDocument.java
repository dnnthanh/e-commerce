package com.dnnthanh.marketplace.be.comment.api.adapter.out.mongo.document;

import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/** Separate reply document to prevent unbounded Mongo thread documents. */
@Document("comment_reply")
public record CommentReplyDocument(
        @Id String id, String threadId, String authorId, String content, LocalDateTime createdAt) {}
