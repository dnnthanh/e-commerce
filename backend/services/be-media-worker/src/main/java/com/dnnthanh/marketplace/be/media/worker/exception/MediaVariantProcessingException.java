package com.dnnthanh.marketplace.be.media.worker.exception;

/** Media transformation or variant persistence failed. */
public final class MediaVariantProcessingException extends RuntimeException {
    public MediaVariantProcessingException(String message) {
        super(message);
    }

    public MediaVariantProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
