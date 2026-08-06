package com.dnnthanh.marketplace.be.checkout.api.adapter.in.web;

import com.dnnthanh.marketplace.be.checkout.api.adapter.in.web.mapper.CheckoutApiMapper;
import com.dnnthanh.marketplace.be.checkout.api.api.CheckoutApi;
import com.dnnthanh.marketplace.be.checkout.api.api.request.CheckoutRequest;
import com.dnnthanh.marketplace.be.checkout.api.api.response.CheckoutResponse;
import com.dnnthanh.marketplace.be.checkout.api.application.port.in.CheckoutUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CheckoutController implements CheckoutApi {
    private final CheckoutUseCase checkout;
    private final CheckoutApiMapper mapper;

    @Override
    public CheckoutResponse checkout(CheckoutRequest request) {
        return mapper.modelToResponse(checkout.checkout(mapper.requestToModel(request)));
    }
}
