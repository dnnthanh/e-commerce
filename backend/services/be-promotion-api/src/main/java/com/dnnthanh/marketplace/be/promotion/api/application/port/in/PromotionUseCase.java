package com.dnnthanh.marketplace.be.promotion.api.application.port.in;

import com.dnnthanh.marketplace.be.promotion.api.application.command.PromotionEvaluationCommand;
import com.dnnthanh.marketplace.be.promotion.api.application.dto.PromotionEvaluationResult;

/** Inbound application port for promotion evaluation. */
public interface PromotionUseCase {
    PromotionEvaluationResult evaluate(PromotionEvaluationCommand command);
}
