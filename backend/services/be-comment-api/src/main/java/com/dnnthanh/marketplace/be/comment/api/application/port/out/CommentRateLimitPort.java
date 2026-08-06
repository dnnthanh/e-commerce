package com.dnnthanh.marketplace.be.comment.api.application.port.out;

/** Anti-spam boundary evaluated before expensive comment writes. */
public interface CommentRateLimitPort {

    /** Throws a stable application error when this user has exceeded the write limit. */
    void checkAllowed(String userId);
}
