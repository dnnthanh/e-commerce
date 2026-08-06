package com.dnnthanh.marketplace.be.checkout.api.adapter.in.web.mapper;

import com.dnnthanh.marketplace.be.checkout.api.api.request.CheckoutItemRequest;
import com.dnnthanh.marketplace.be.checkout.api.api.request.CheckoutRequest;
import com.dnnthanh.marketplace.be.checkout.api.api.response.CheckoutResponse;
import com.dnnthanh.marketplace.be.checkout.api.application.model.CheckoutCommand;
import com.dnnthanh.marketplace.be.checkout.api.application.model.CheckoutItemCommand;
import com.dnnthanh.marketplace.be.checkout.api.application.model.CheckoutResult;
import com.dnnthanh.marketplace.be.platform.mapping.ModelResponseMapper;
import com.dnnthanh.marketplace.be.platform.mapping.PlatformMapperConfig;
import com.dnnthanh.marketplace.be.platform.mapping.RequestModelMapper;
import org.mapstruct.Mapper;

@Mapper(config = PlatformMapperConfig.class)
public interface CheckoutApiMapper
        extends RequestModelMapper<CheckoutRequest, CheckoutCommand>,
                ModelResponseMapper<CheckoutResult, CheckoutResponse> {
    @Override
    CheckoutCommand requestToModel(CheckoutRequest request);

    CheckoutItemCommand toCommand(CheckoutItemRequest request);

    @Override
    CheckoutResponse modelToResponse(CheckoutResult result);
}
