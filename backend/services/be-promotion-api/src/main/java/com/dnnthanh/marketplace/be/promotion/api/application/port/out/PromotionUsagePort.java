package com.dnnthanh.marketplace.be.promotion.api.application.port.out;

/** Atomic promotion usage reservation boundary. */
public interface PromotionUsagePort {
    boolean reserve(
            String reservationKey,
            String promotionId,
            String customerId,
            long globalLimit,
            long customerLimit);

    void confirm(String reservationKey);

    void release(String reservationKey);
}
