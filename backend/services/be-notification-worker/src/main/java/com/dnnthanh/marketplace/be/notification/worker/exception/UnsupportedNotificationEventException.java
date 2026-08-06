package com.dnnthanh.marketplace.be.notification.worker.exception;

/** A source event is recognized by the listener but has no notification mapping. */
public final class UnsupportedNotificationEventException extends RuntimeException {
    public UnsupportedNotificationEventException(String eventType) {
        super("Unsupported payment notification event " + eventType);
    }
}
