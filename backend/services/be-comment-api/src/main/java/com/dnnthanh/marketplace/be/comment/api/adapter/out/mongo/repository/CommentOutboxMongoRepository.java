package com.dnnthanh.marketplace.be.comment.api.adapter.out.mongo.repository;

import com.dnnthanh.marketplace.be.comment.api.adapter.out.mongo.document.CommentOutboxDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

/** Spring Data Mongo repository for comment outbox records. */
public interface CommentOutboxMongoRepository
        extends MongoRepository<CommentOutboxDocument, String> {}
