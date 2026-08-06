package com.dnnthanh.marketplace.be.audit.api.application.exception;

import com.dnnthanh.marketplace.be.platform.exception.BusinessException;

/** Raised when an audit search violates bounded-query constraints. */
public class InvalidAuditSearchCriteriaException extends BusinessException {
    public InvalidAuditSearchCriteriaException(String message) {
        super(AuditErrorCode.INVALID_AUDIT_SEARCH_CRITERIA, message);
    }
}
