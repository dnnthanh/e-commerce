package com.dnnthanh.marketplace.be.comment.api.api;

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
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

/** Interface-driven public/private moderation-aware Comment API. */
public interface CommentApi {

    /** Lists product threads with pageable roots. */
    @GetMapping("/comments")
    Page<CommentThreadResponse> list(
            @Valid @ModelAttribute CommentSearchRequest request, Pageable pageable);

    /** Lists replies separately for one thread. */
    @GetMapping("/comments/{threadId}/replies")
    Page<CommentReplyResponse> replies(@PathVariable String threadId, Pageable pageable);

    @PostMapping("/private/comments")
    @PreAuthorize("@authorizationService.hasPermission('COMMENT_CREATE')")
    CommentThreadResponse create(@Valid @RequestBody CreateCommentRequest request);

    /** Replies to a thread. */
    @PostMapping("/private/comments/{threadId}/replies")
    @PreAuthorize("@authorizationService.hasPermission('COMMENT_CREATE')")
    CommentReplyResponse reply(
            @PathVariable String threadId, @Valid @RequestBody ReplyCommentRequest request);

    /** Edits the authenticated user's own thread. */
    @PutMapping("/private/comments/{threadId}")
    @PreAuthorize("@authorizationService.hasPermission('COMMENT_EDIT')")
    CommentThreadResponse edit(
            @PathVariable String threadId, @Valid @RequestBody EditCommentRequest request);

    /** Soft deletes the authenticated user's root while retaining descendants. */
    @DeleteMapping("/private/comments/{threadId}")
    @PreAuthorize("@authorizationService.hasPermission('COMMENT_DELETE')")
    CommentThreadResponse delete(@PathVariable String threadId);

    /** Adds an idempotent reaction. */
    @PutMapping("/private/comments/{threadId}/reaction")
    @PreAuthorize("@authorizationService.hasPermission('COMMENT_REACT')")
    CommentReactionResponse react(
            @PathVariable String threadId, @Valid @RequestBody CommentReactionRequest request);

    /** Removes a reaction idempotently. */
    @DeleteMapping("/private/comments/{threadId}/reaction")
    @PreAuthorize("@authorizationService.hasPermission('COMMENT_REACT')")
    void removeReaction(
            @PathVariable String threadId, @Valid @RequestBody CommentReactionRequest request);

    /** Reports a thread for moderation. */
    @PostMapping("/private/comments/{threadId}/reports")
    @PreAuthorize("@authorizationService.hasPermission('COMMENT_REPORT')")
    CommentReportResponse report(
            @PathVariable String threadId, @Valid @RequestBody CommentReportRequest request);

    /** Hides a thread for internal callers or an authorized browser moderator. */
    @PostMapping({"/internal/comments/{threadId}/hide", "/private/comments/{threadId}/hide"})
    @PreAuthorize("@authorizationService.hasPermission('COMMENT_MODERATE')")
    CommentThreadResponse hide(@PathVariable String threadId);

    /**
     * Restores a moderation-hidden thread for internal callers or an authorized browser moderator.
     */
    @PostMapping({"/internal/comments/{threadId}/unhide", "/private/comments/{threadId}/unhide"})
    @PreAuthorize("@authorizationService.hasPermission('COMMENT_MODERATE')")
    CommentThreadResponse unhide(@PathVariable String threadId);
}
