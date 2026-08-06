package com.dnnthanh.marketplace.be.comment.api.adapter.in.web.mapper;

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
import com.dnnthanh.marketplace.be.comment.api.application.command.CommentReactionCommand;
import com.dnnthanh.marketplace.be.comment.api.application.command.CreateCommentCommand;
import com.dnnthanh.marketplace.be.comment.api.application.command.EditCommentCommand;
import com.dnnthanh.marketplace.be.comment.api.application.command.ReplyCommentCommand;
import com.dnnthanh.marketplace.be.comment.api.application.command.ReportCommentCommand;
import com.dnnthanh.marketplace.be.comment.api.application.query.CommentSearchCriteria;
import com.dnnthanh.marketplace.be.comment.api.domain.model.CommentReaction;
import com.dnnthanh.marketplace.be.comment.api.domain.model.CommentReply;
import com.dnnthanh.marketplace.be.comment.api.domain.model.CommentReport;
import com.dnnthanh.marketplace.be.comment.api.domain.model.CommentThread;
import com.dnnthanh.marketplace.be.platform.mapping.MapperContract;
import com.dnnthanh.marketplace.be.platform.mapping.PlatformMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** Maps Comment transport, application commands/criteria and domain responses. */
@Mapper(config = PlatformMapperConfig.class)
public interface CommentApiMapper extends MapperContract {

    CommentSearchCriteria toCriteria(CommentSearchRequest request);

    CreateCommentCommand toCommand(CreateCommentRequest request);

    @Mapping(target = "threadId", source = "threadId")
    @Mapping(target = "content", source = "request.content")
    ReplyCommentCommand toCommand(String threadId, ReplyCommentRequest request);

    @Mapping(target = "threadId", source = "threadId")
    @Mapping(target = "content", source = "request.content")
    EditCommentCommand toCommand(String threadId, EditCommentRequest request);

    @Mapping(target = "threadId", source = "threadId")
    @Mapping(target = "type", source = "request.type")
    CommentReactionCommand toCommand(String threadId, CommentReactionRequest request);

    @Mapping(target = "threadId", source = "threadId")
    @Mapping(target = "reason", source = "request.reason")
    @Mapping(target = "details", source = "request.details")
    ReportCommentCommand toCommand(String threadId, CommentReportRequest request);

    @Mapping(target = "content", source = "visibleContent")
    CommentThreadResponse toResponse(CommentThread thread);

    CommentReplyResponse toResponse(CommentReply reply);

    CommentReactionResponse toResponse(CommentReaction reaction);

    CommentReportResponse toResponse(CommentReport report);
}
