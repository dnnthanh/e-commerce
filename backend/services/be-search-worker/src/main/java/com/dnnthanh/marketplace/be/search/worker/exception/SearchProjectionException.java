package com.dnnthanh.marketplace.be.search.worker.exception;

/** Authoritative source data required for a search projection is unavailable or invalid. */
public final class SearchProjectionException extends RuntimeException {
    public SearchProjectionException(String message) {
        super(message);
    }
}
