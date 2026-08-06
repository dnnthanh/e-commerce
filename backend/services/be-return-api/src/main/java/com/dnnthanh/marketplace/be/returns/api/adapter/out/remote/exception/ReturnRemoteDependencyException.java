package com.dnnthanh.marketplace.be.returns.api.adapter.out.remote.exception;

/** Infrastructure failure while reading an authoritative remote return dependency. */
public final class ReturnRemoteDependencyException extends RuntimeException {
    public ReturnRemoteDependencyException(String message, Throwable cause) {
        super(message, cause);
    }
}
