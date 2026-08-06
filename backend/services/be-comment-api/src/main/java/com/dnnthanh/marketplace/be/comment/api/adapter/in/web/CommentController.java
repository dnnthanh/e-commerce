package com.dnnthanh.marketplace.be.comment.api.adapter.in.web;

import com.dnnthanh.marketplace.be.comment.api.adapter.in.web.mapper.CommentApiMapper;
import com.dnnthanh.marketplace.be.comment.api.api.CommentApi;
import com.dnnthanh.marketplace.be.comment.api.api.request.CommentReactionRequest;
import com.dnnthanh.marketplace.be.comment.api.api.request.CommentReportRequest;
import com.dnnthanh.marketplace.be.comment.api.api.request.CreateCommentRequest;
import com.dnnthanh.marketplace.be.comment.api.api.request.EditCommentRequest;
import com.dnnthanh.marketplace.be.comment.api.api.request.ReplyCommentRequest;
import com.dnnthanh.marketplace.be.comment.api.api.request.search.CommentSearchRequest;
import com.dnnthanh.marketplace.be.comment.api.api.response.CommentReactionResponse;
import com.dnnthanh.marketplace.be.comment.api.api.response.CommentReplyResponse;
import com.dnnthanh.marketplace.be.comment.api.api.response.CommentReportResponse;
import com.dnnthanh.marketplace.be.comment.api.api.response.CommentThreadResponse;
import com.dnnthanh.marketplace.be.comment.api.application.port.in.CommentCommandUseCase;
import com.dnnthanh.marketplace.be.comment.api.application.port.in.CommentQueryUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CommentController implements CommentApi {

    private final CommentCommandUseCase commandUseCase;
    private final CommentQueryUseCase queryUseCase;
    private final CommentApiMapper mapper;

    @Override
    public Page<CommentThreadResponse> list(CommentSearchRequest request, Pageable pageable) {
        return queryUseCase
                .listThreads(mapper.toCriteria(request), pageable)
                .map(mapper::toResponse);
    }

    @Override
    public Page<CommentReplyResponse> replies(String threadId, Pageable pageable) {
        return queryUseCase.listReplies(threadId, pageable).map(mapper::toResponse);
    }

    @Override
    public CommentThreadResponse create(CreateCommentRequest request) {
        return mapper.toResponse(commandUseCase.create(mapper.toCommand(request)));
    }

    @Override
    public CommentReplyResponse reply(String threadId, ReplyCommentRequest request) {
        return mapper.toResponse(commandUseCase.reply(mapper.toCommand(threadId, request)));
    }

    @Override
    public CommentThreadResponse edit(String threadId, EditCommentRequest request) {
        return mapper.toResponse(commandUseCase.edit(mapper.toCommand(threadId, request)));
    }

    @Override
    public CommentThreadResponse delete(String threadId) {
        return mapper.toResponse(commandUseCase.delete(threadId));
    }

    @Override
    public CommentReactionResponse react(String threadId, CommentReactionRequest request) {
        return mapper.toResponse(commandUseCase.react(mapper.toCommand(threadId, request)));
    }

    @Override
    public void removeReaction(String threadId, CommentReactionRequest request) {
        commandUseCase.removeReaction(mapper.toCommand(threadId, request));
    }

    @Override
    public CommentReportResponse report(String threadId, CommentReportRequest request) {
        return mapper.toResponse(commandUseCase.report(mapper.toCommand(threadId, request)));
    }

    @Override
    public CommentThreadResponse hide(String threadId) {
        return mapper.toResponse(commandUseCase.hide(threadId));
    }

    @Override
    public CommentThreadResponse unhide(String threadId) {
        return mapper.toResponse(commandUseCase.unhide(threadId));
    }
}
