package com.dnnthanh.marketplace.be.promotion.api.adapter.in.web;

import com.dnnthanh.marketplace.be.promotion.api.adapter.in.web.mapper.PromotionApiMapper;
import com.dnnthanh.marketplace.be.promotion.api.api.PromotionApi;
import com.dnnthanh.marketplace.be.promotion.api.api.request.PromotionRequest;
import com.dnnthanh.marketplace.be.promotion.api.api.response.PromotionResult;
import com.dnnthanh.marketplace.be.promotion.api.application.port.in.PromotionUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PromotionController implements PromotionApi {
    private final PromotionUseCase useCase;
    private final PromotionApiMapper mapper;

    @Override
    public PromotionResult evaluate(PromotionRequest request) {
        return mapper.toResponse(useCase.evaluate(mapper.toCommand(request)));
    }
}
