package com.dnnthanh.marketplace.be.media.api.adapter.out.persistence.exception;

/** Infrastructure failure while persisting media metadata or its transactional outbox event. */
public class MediaMetadataPersistenceException extends RuntimeException {
    public MediaMetadataPersistenceException(String message) {
        super(message);
    }
}
