package com.dnnthanh.marketplace.be.promotion.api.application.exception;

/** Promotion reservation lost a race against a global/customer usage limit. */
public final class PromotionUsageLimitExceededException extends RuntimeException {
    public PromotionUsageLimitExceededException(String promotionId) {
        super("Promotion usage limit reached: " + promotionId);
    }
}
