package com.dnnthanh.marketplace.be.platform.exception;

/** Raised when the internal client-credentials token cannot be acquired safely. */
public final class ServiceTokenAcquisitionException extends BusinessException {

    public ServiceTokenAcquisitionException(String diagnosticMessage) {
        super(PlatformErrorCode.SERVICE_TOKEN_ACQUISITION_FAILED, diagnosticMessage);
    }

    public ServiceTokenAcquisitionException(String diagnosticMessage, Throwable cause) {
        super(
                PlatformErrorCode.SERVICE_TOKEN_ACQUISITION_FAILED,
                diagnosticMessage,
                null,
                null,
                cause);
    }
}
