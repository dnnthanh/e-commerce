package com.dnnthanh.marketplace.be.comment.api.application.port.in;

import com.dnnthanh.marketplace.be.comment.api.application.command.CommentReactionCommand;
import com.dnnthanh.marketplace.be.comment.api.application.command.CreateCommentCommand;
import com.dnnthanh.marketplace.be.comment.api.application.command.EditCommentCommand;
import com.dnnthanh.marketplace.be.comment.api.application.command.ReplyCommentCommand;
import com.dnnthanh.marketplace.be.comment.api.application.command.ReportCommentCommand;
import com.dnnthanh.marketplace.be.comment.api.domain.model.CommentReaction;
import com.dnnthanh.marketplace.be.comment.api.domain.model.CommentReply;
import com.dnnthanh.marketplace.be.comment.api.domain.model.CommentReport;
import com.dnnthanh.marketplace.be.comment.api.domain.model.CommentThread;

public interface CommentCommandUseCase {
    CommentThread create(CreateCommentCommand command);

    CommentReply reply(ReplyCommentCommand command);

    CommentThread edit(EditCommentCommand command);

    CommentThread delete(String threadId);

    CommentThread hide(String threadId);

    CommentThread unhide(String threadId);

    CommentReport report(ReportCommentCommand command);

    CommentReaction react(CommentReactionCommand command);

    boolean removeReaction(CommentReactionCommand command);
}
