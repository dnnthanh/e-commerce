package com.dnnthanh.marketplace.be.checkout.api.adapter.in.web;

import com.dnnthanh.marketplace.be.checkout.api.adapter.in.web.mapper.CheckoutApiMapper;
import com.dnnthanh.marketplace.be.checkout.api.api.CheckoutInternalApi;
import com.dnnthanh.marketplace.be.checkout.api.api.response.CheckoutResponse;
import com.dnnthanh.marketplace.be.checkout.api.application.port.in.CheckoutRecoveryUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CheckoutInternalController implements CheckoutInternalApi {
    private final CheckoutRecoveryUseCase recovery;
    private final CheckoutApiMapper mapper;

    @Override
    public CheckoutResponse recover(String checkoutKey) {
        return mapper.modelToResponse(recovery.recover(checkoutKey));
    }
}
