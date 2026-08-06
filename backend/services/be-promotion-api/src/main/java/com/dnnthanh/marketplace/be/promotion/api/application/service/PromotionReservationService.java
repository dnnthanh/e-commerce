package com.dnnthanh.marketplace.be.promotion.api.application.service;

import com.dnnthanh.marketplace.be.platform.stereotype.UseCase;
import com.dnnthanh.marketplace.be.promotion.api.application.exception.PromotionUsageLimitExceededException;
import com.dnnthanh.marketplace.be.promotion.api.application.port.out.PromotionUsagePort;
import com.dnnthanh.marketplace.be.promotion.api.domain.model.PromotionCandidate;
import com.dnnthanh.marketplace.be.promotion.api.domain.service.PromotionEngine;
import java.util.List;
import lombok.RequiredArgsConstructor;

/** Evaluates promotions and atomically reserves usages selected for checkout. */
@UseCase
@RequiredArgsConstructor
public class PromotionReservationService {
    private final PromotionEngine engine;
    private final PromotionUsagePort usagePort;

    public PromotionEngine.Evaluation evaluate(
            List<PromotionCandidate> candidates, PromotionEngine.PromotionContext context) {
        return engine.evaluate(candidates, context);
    }

    public void reserve(String checkoutId, String customerId, List<PromotionCandidate> applied) {
        for (PromotionCandidate promotion : applied) {
            String key = checkoutId + ":" + promotion.promotionId();
            boolean accepted =
                    usagePort.reserve(
                            key,
                            promotion.promotionId(),
                            customerId,
                            promotion.globalLimit(),
                            promotion.perCustomerLimit());
            if (!accepted) throw new PromotionUsageLimitExceededException(promotion.promotionId());
        }
    }

    public void confirm(String checkoutId, List<String> promotionIds) {
        promotionIds.forEach(id -> usagePort.confirm(checkoutId + ":" + id));
    }

    public void compensate(String checkoutId, List<String> promotionIds) {
        promotionIds.forEach(id -> usagePort.release(checkoutId + ":" + id));
    }
}
