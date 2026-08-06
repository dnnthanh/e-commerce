package com.dnnthanh.marketplace.be.media.api.domain.exception;

/** Media processing/attachment command conflicts with asset lifecycle state. */
public final class MediaStateConflictException extends RuntimeException {
    public MediaStateConflictException(String message) {
        super(message);
    }
}
