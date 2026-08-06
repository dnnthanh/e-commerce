package com.dnnthanh.marketplace.be.comment.api.adapter.out.mongo.repository;

import com.dnnthanh.marketplace.be.comment.api.adapter.out.mongo.document.CommentReactionDocument;
import com.dnnthanh.marketplace.be.comment.api.domain.enumtype.ReactionType;
import org.springframework.data.mongodb.repository.MongoRepository;

/** Spring Data Mongo repository for idempotent comment reactions. */
public interface CommentReactionMongoRepository
        extends MongoRepository<CommentReactionDocument, String> {
    long deleteByThreadIdAndUserIdAndType(String threadId, String userId, ReactionType type);
}
