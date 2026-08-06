package com.dnnthanh.marketplace.be.comment.api.application.port.out;

import com.dnnthanh.marketplace.be.comment.api.application.query.CommentSearchCriteria;
import com.dnnthanh.marketplace.be.comment.api.domain.enumtype.CommentEventType;
import com.dnnthanh.marketplace.be.comment.api.domain.enumtype.ReactionType;
import com.dnnthanh.marketplace.be.comment.api.domain.model.CommentReaction;
import com.dnnthanh.marketplace.be.comment.api.domain.model.CommentReply;
import com.dnnthanh.marketplace.be.comment.api.domain.model.CommentReport;
import com.dnnthanh.marketplace.be.comment.api.domain.model.CommentThread;
import java.util.Map;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Mongo persistence boundary for comment threads, replies, reactions, reports and outbox records.
 */
public interface CommentPersistencePort {

    /** Finds one root thread by its durable identifier. */
    Optional<CommentThread> findThread(String threadId);

    /** Inserts or updates the root thread aggregate. */
    CommentThread saveThread(CommentThread thread);

    /** Inserts one reply document owned by the supplied thread. */
    CommentReply insertReply(CommentReply reply);

    /** Returns product threads using the requested page and sort. */
    Page<CommentThread> findThreads(CommentSearchCriteria criteria, Pageable pageable);

    /** Returns replies for one thread using the requested page and sort. */
    Page<CommentReply> findReplies(String threadId, Pageable pageable);

    /** Adds a deterministic thread/user/type reaction idempotently. */
    CommentReaction addReaction(CommentReaction reaction);

    /** Removes a deterministic reaction if it exists. */
    boolean removeReaction(String threadId, String userId, ReactionType type);

    /** Persists one moderation report. */
    CommentReport insertReport(CommentReport report);

    /** Appends a durable comment outbox record in the same Mongo transaction. */
    void appendOutbox(CommentEventType eventType, String aggregateId, Map<String, ?> payload);
}
