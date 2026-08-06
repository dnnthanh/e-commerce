package com.dnnthanh.marketplace.be.comment.api.adapter.out.mongo.repository;

import com.dnnthanh.marketplace.be.comment.api.adapter.out.mongo.document.CommentThreadDocument;
import com.dnnthanh.marketplace.be.comment.api.domain.enumtype.CommentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

/** Spring Data Mongo repository for thread roots. */
public interface CommentThreadMongoRepository
        extends MongoRepository<CommentThreadDocument, String> {
    Page<CommentThreadDocument> findByProductIdAndStatusNot(
            Long productId, CommentStatus excludedStatus, Pageable pageable);
}
