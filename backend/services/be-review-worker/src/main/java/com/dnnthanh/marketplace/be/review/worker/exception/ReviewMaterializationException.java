package com.dnnthanh.marketplace.be.review.worker.exception;

/** Delivered-order data cannot be materialized into verified-purchase review eligibility. */
public final class ReviewMaterializationException extends RuntimeException {
    public ReviewMaterializationException(String message) {
        super(message);
    }
}
