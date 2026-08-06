package com.dnnthanh.marketplace.be.promotion.api.application.service;

import com.dnnthanh.marketplace.be.platform.stereotype.UseCase;
import com.dnnthanh.marketplace.be.promotion.api.application.command.PromotionEvaluationCommand;
import com.dnnthanh.marketplace.be.promotion.api.application.dto.PromotionEvaluationResult;
import com.dnnthanh.marketplace.be.promotion.api.application.dto.PromotionReservationResult;
import com.dnnthanh.marketplace.be.promotion.api.application.exception.PromotionUsageLimitExceededException;
import com.dnnthanh.marketplace.be.promotion.api.application.port.in.PromotionCheckoutReservationUseCase;
import com.dnnthanh.marketplace.be.promotion.api.application.port.in.PromotionUseCase;
import com.dnnthanh.marketplace.be.promotion.api.application.port.out.PromotionRepositoryPort;
import com.dnnthanh.marketplace.be.promotion.api.application.port.out.PromotionUsagePort;
import com.dnnthanh.marketplace.be.promotion.api.domain.model.Promotion;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

/** Atomically reserves the exact promotion evaluation selected for one Checkout idempotency key. */
@UseCase
@RequiredArgsConstructor
public class PromotionCheckoutReservationServiceImplement
        implements PromotionCheckoutReservationUseCase {
    private final PromotionUseCase promotionService;
    private final PromotionRepositoryPort promotionRepository;
    private final PromotionUsagePort usagePort;

    @Override
    public PromotionReservationResult reserve(
            String checkoutKey, String customerId, BigDecimal subtotal, List<String> codes) {
        PromotionEvaluationResult evaluation =
                promotionService.evaluate(PromotionEvaluationCommand.checkout(subtotal, codes));
        Map<String, Promotion> activeById =
                promotionRepository.findActive(LocalDateTime.now()).stream()
                        .collect(
                                Collectors.toMap(
                                        promotion -> String.valueOf(promotion.id()),
                                        Function.identity()));
        List<String> reserved = new ArrayList<>();
        try {
            for (var applied : evaluation.applied()) {
                Promotion promotion = activeById.get(applied.promotionId());
                if (promotion == null) {
                    throw new PromotionUsageLimitExceededException(applied.promotionId());
                }
                String reservationKey = reservationKey(checkoutKey, applied.promotionId());
                if (!usagePort.reserve(
                        reservationKey,
                        applied.promotionId(),
                        customerId,
                        promotion.globalLimit(),
                        promotion.perCustomerLimit())) {
                    throw new PromotionUsageLimitExceededException(applied.promotionId());
                }
                reserved.add(applied.promotionId());
            }
            return new PromotionReservationResult(evaluation.totalDiscount(), evaluation.applied());
        } catch (RuntimeException failure) {
            reserved.forEach(id -> usagePort.release(reservationKey(checkoutKey, id)));
            throw failure;
        }
    }

    @Override
    public void confirm(String checkoutKey, List<String> promotionIds) {
        promotionIds.forEach(id -> usagePort.confirm(reservationKey(checkoutKey, id)));
    }

    @Override
    public void release(String checkoutKey, List<String> promotionIds) {
        promotionIds.forEach(id -> usagePort.release(reservationKey(checkoutKey, id)));
    }

    private static String reservationKey(String checkoutKey, String promotionId) {
        return checkoutKey + ":" + promotionId;
    }
}
