package com.dnnthanh.marketplace.be.comment.api.adapter.out.mongo.repository;

import com.dnnthanh.marketplace.be.comment.api.adapter.out.mongo.document.CommentReportDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

/** Spring Data Mongo repository for moderation reports. */
public interface CommentReportMongoRepository
        extends MongoRepository<CommentReportDocument, String> {}
