package com.dnnthanh.marketplace.be.comment.api.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.dnnthanh.marketplace.be.comment.api.domain.enumtype.CommentStatus;
import com.dnnthanh.marketplace.be.comment.api.domain.exception.CommentPermissionException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

/** Domain tests for author editing, moderation and soft deletion. */
class CommentThreadTest {

    @Test
    void authorCanEditOwnPublishedComment() {
        CommentThread thread = newThread();
        thread.edit("user-1", "updated content", LocalDateTime.now());
        assertEquals("updated content", thread.getContent());
        assertEquals(CommentStatus.EDITED, thread.getStatus());
    }

    @Test
    void anotherUserCannotEditComment() {
        CommentThread thread = newThread();
        assertThrows(
                CommentPermissionException.class,
                () -> thread.edit("user-2", "hijack", LocalDateTime.now()));
    }

    @Test
    void softDeletePreservesReplyCount() {
        CommentThread thread = newThread();
        thread.recordReply(LocalDateTime.now());
        thread.softDelete("user-1", LocalDateTime.now());
        assertEquals(CommentStatus.DELETED, thread.getStatus());
        assertEquals(1, thread.getReplyCount());
    }

    @Test
    void moderationCanHideAndUnhideWithoutDestroyingContent() {
        CommentThread thread = newThread();
        thread.hide(LocalDateTime.now());
        thread.unhide(LocalDateTime.now());
        assertEquals(CommentStatus.PUBLISHED, thread.getStatus());
        assertEquals("hello", thread.getContent());
    }

    private CommentThread newThread() {
        return CommentThread.create(
                CommentThreadDraft.builder()
                        .productId(1L)
                        .sellerId(10L)
                        .authorId("user-1")
                        .content("hello")
                        .build(),
                LocalDateTime.now());
    }
}
