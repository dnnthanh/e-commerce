package com.dnnthanh.marketplace.be.comment.api.application.service;

import com.dnnthanh.marketplace.be.comment.api.application.exception.CommentNotFoundException;
import com.dnnthanh.marketplace.be.comment.api.application.port.in.CommentQueryUseCase;
import com.dnnthanh.marketplace.be.comment.api.application.port.out.CommentPersistencePort;
import com.dnnthanh.marketplace.be.comment.api.application.query.CommentSearchCriteria;
import com.dnnthanh.marketplace.be.comment.api.domain.model.CommentReply;
import com.dnnthanh.marketplace.be.comment.api.domain.model.CommentThread;
import com.dnnthanh.marketplace.be.platform.stereotype.UseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

/** Read-side Comment use cases with explicit thread/reply pagination. */
@UseCase
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentQueryServiceImplement implements CommentQueryUseCase {

    private final CommentPersistencePort persistence;

    /** Lists visible/deleted-placeholder product threads. */
    public Page<CommentThread> listThreads(CommentSearchCriteria criteria, Pageable pageable) {
        return persistence.findThreads(criteria, pageable);
    }

    /** Lists replies separately to avoid unbounded root documents and response payloads. */
    public Page<CommentReply> listReplies(String threadId, Pageable pageable) {
        if (persistence.findThread(threadId).isEmpty()) {
            throw new CommentNotFoundException();
        }
        return persistence.findReplies(threadId, pageable);
    }
}
