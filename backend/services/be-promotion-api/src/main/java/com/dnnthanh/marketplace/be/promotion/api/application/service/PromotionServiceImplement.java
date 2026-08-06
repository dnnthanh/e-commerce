package com.dnnthanh.marketplace.be.promotion.api.application.service;

import com.dnnthanh.marketplace.be.platform.stereotype.UseCase;
import com.dnnthanh.marketplace.be.promotion.api.application.command.PromotionEvaluationCommand;
import com.dnnthanh.marketplace.be.promotion.api.application.dto.AppliedPromotionDto;
import com.dnnthanh.marketplace.be.promotion.api.application.dto.PromotionEvaluationResult;
import com.dnnthanh.marketplace.be.promotion.api.application.port.in.PromotionUseCase;
import com.dnnthanh.marketplace.be.promotion.api.application.port.out.PromotionRepositoryPort;
import com.dnnthanh.marketplace.be.promotion.api.domain.model.Promotion;
import com.dnnthanh.marketplace.be.promotion.api.domain.model.Promotion.PromotionEvaluationContext;
import com.dnnthanh.marketplace.be.promotion.api.domain.model.Promotion.PromotionLine;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

/** Promotion evaluation with targeting, explainable stacking and deterministic priority. */
@UseCase
@RequiredArgsConstructor
public class PromotionServiceImplement implements PromotionUseCase {
    private final PromotionRepositoryPort repository;

    @Override
    public PromotionEvaluationResult evaluate(PromotionEvaluationCommand command) {
        BigDecimal subtotal = command.subtotal() == null ? BigDecimal.ZERO : command.subtotal();
        LocalDateTime now = LocalDateTime.now();
        PromotionEvaluationContext context =
                new PromotionEvaluationContext(
                        subtotal,
                        command.lines().stream()
                                .map(
                                        line ->
                                                new PromotionLine(
                                                        line.sellerId(),
                                                        line.skuId(),
                                                        line.categoryId(),
                                                        line.subtotal()))
                                .toList(),
                        command.channel(),
                        command.customerSegment(),
                        now);
        Set<String> requestedCodes =
                command.codes().stream().collect(Collectors.toUnmodifiableSet());
        Map<String, Promotion> grouped = new LinkedHashMap<>();
        List<Promotion> selected = new ArrayList<>();

        for (Promotion promotion : repository.findActive(now)) {
            if (!requestedCodes.isEmpty() && !requestedCodes.contains(promotion.code())) {
                continue;
            }
            if (!promotion.eligible(context)) {
                continue;
            }
            if (promotion.stackingGroup() == null || promotion.stackingGroup().isBlank()) {
                selected.add(promotion);
            } else {
                grouped.merge(
                        promotion.stackingGroup(),
                        promotion,
                        (left, right) -> left.priority() >= right.priority() ? left : right);
            }
        }
        selected.addAll(grouped.values());
        selected.sort((left, right) -> Integer.compare(right.priority(), left.priority()));

        List<AppliedPromotionDto> applied = new ArrayList<>();
        BigDecimal remaining = subtotal;
        for (Promotion promotion : selected) {
            BigDecimal eligibleBase =
                    promotion.eligibleSubtotal(context.lines(), remaining.min(subtotal));
            BigDecimal discount = promotion.discount(eligibleBase).min(remaining);
            if (discount.signum() > 0) {
                applied.add(
                        new AppliedPromotionDto(
                                String.valueOf(promotion.id()), promotion.code(), discount));
                remaining = remaining.subtract(discount);
            }
        }
        return new PromotionEvaluationResult(
                subtotal, subtotal.subtract(remaining), remaining, List.copyOf(applied));
    }
}
