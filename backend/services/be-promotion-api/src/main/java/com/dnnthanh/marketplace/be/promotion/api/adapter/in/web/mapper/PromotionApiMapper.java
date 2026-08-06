package com.dnnthanh.marketplace.be.promotion.api.adapter.in.web.mapper;

import com.dnnthanh.marketplace.be.platform.mapping.MapperContract;
import com.dnnthanh.marketplace.be.platform.mapping.PlatformMapperConfig;
import com.dnnthanh.marketplace.be.promotion.api.api.request.PromotionLineRequest;
import com.dnnthanh.marketplace.be.promotion.api.api.request.PromotionRequest;
import com.dnnthanh.marketplace.be.promotion.api.api.response.AppliedPromotion;
import com.dnnthanh.marketplace.be.promotion.api.api.response.PromotionResult;
import com.dnnthanh.marketplace.be.promotion.api.application.command.PromotionEvaluationCommand;
import com.dnnthanh.marketplace.be.promotion.api.application.dto.AppliedPromotionDto;
import com.dnnthanh.marketplace.be.promotion.api.application.dto.PromotionEvaluationResult;
import org.mapstruct.Mapper;

/** Maps HTTP promotion contracts to application commands/results. */
@Mapper(config = PlatformMapperConfig.class)
public interface PromotionApiMapper extends MapperContract {
    PromotionEvaluationCommand toCommand(PromotionRequest request);

    PromotionEvaluationCommand.Line toCommand(PromotionLineRequest request);

    PromotionResult toResponse(PromotionEvaluationResult result);

    AppliedPromotion toResponse(AppliedPromotionDto applied);
}
