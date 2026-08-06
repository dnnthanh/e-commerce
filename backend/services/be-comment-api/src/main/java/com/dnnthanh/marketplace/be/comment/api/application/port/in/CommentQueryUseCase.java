package com.dnnthanh.marketplace.be.comment.api.application.port.in;

import com.dnnthanh.marketplace.be.comment.api.application.query.CommentSearchCriteria;
import com.dnnthanh.marketplace.be.comment.api.domain.model.CommentReply;
import com.dnnthanh.marketplace.be.comment.api.domain.model.CommentThread;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommentQueryUseCase {
    Page<CommentThread> listThreads(CommentSearchCriteria criteria, Pageable pageable);

    Page<CommentReply> listReplies(String threadId, Pageable pageable);
}
