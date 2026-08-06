package com.dnnthanh.marketplace.be.promotion.api.adapter.in.web;

import com.dnnthanh.marketplace.be.promotion.api.adapter.in.web.mapper.PromotionApiMapper;
import com.dnnthanh.marketplace.be.promotion.api.api.PromotionInternalApi;
import com.dnnthanh.marketplace.be.promotion.api.api.request.ReservationMutationRequest;
import com.dnnthanh.marketplace.be.promotion.api.api.request.ReservePromotionRequest;
import com.dnnthanh.marketplace.be.promotion.api.api.response.ReservePromotionResponse;
import com.dnnthanh.marketplace.be.promotion.api.application.port.in.PromotionCheckoutReservationUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PromotionInternalController implements PromotionInternalApi {
    private final PromotionCheckoutReservationUseCase useCase;
    private final PromotionApiMapper mapper;

    @Override
    public ReservePromotionResponse reserve(ReservePromotionRequest request) {
        var result =
                useCase.reserve(
                        request.checkoutKey(),
                        request.customerId(),
                        request.subtotal(),
                        request.codes());
        var applied = result.applied().stream().map(mapper::toResponse).toList();
        return new ReservePromotionResponse(
                result.totalDiscount(),
                result.applied().stream().map(a -> a.promotionId()).toList(),
                applied);
    }

    @Override
    public void confirm(String checkoutKey, ReservationMutationRequest request) {
        useCase.confirm(checkoutKey, request.promotionIds());
    }

    @Override
    public void release(String checkoutKey, ReservationMutationRequest request) {
        useCase.release(checkoutKey, request.promotionIds());
    }
}
