package com.dnnthanh.marketplace.be.search.api.application.exception;

import com.dnnthanh.marketplace.be.platform.exception.BusinessException;

/** Search request violates cursor/page/range constraints. */
public final class InvalidSearchCriteriaException extends BusinessException {
    public InvalidSearchCriteriaException(String message) {
        super(SearchErrorCode.INVALID_SEARCH_CRITERIA, message);
    }
}
