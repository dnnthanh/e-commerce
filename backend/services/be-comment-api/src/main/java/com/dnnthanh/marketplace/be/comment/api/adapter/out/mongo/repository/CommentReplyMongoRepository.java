package com.dnnthanh.marketplace.be.comment.api.adapter.out.mongo.repository;

import com.dnnthanh.marketplace.be.comment.api.adapter.out.mongo.document.CommentReplyDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

/** Spring Data Mongo repository for separately paged replies. */
public interface CommentReplyMongoRepository extends MongoRepository<CommentReplyDocument, String> {
    Page<CommentReplyDocument> findByThreadId(String threadId, Pageable pageable);
}
