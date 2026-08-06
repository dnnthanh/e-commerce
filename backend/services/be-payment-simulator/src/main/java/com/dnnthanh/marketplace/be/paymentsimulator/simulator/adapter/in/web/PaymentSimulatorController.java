package com.dnnthanh.marketplace.be.paymentsimulator.simulator.adapter.in.web;

import com.dnnthanh.marketplace.be.paymentsimulator.simulator.api.PaymentSimulatorApi;
import com.dnnthanh.marketplace.be.paymentsimulator.simulator.api.request.ProviderCommand;
import com.dnnthanh.marketplace.be.paymentsimulator.simulator.api.request.RefundCommand;
import com.dnnthanh.marketplace.be.paymentsimulator.simulator.api.response.ProviderResult;
import com.dnnthanh.marketplace.be.paymentsimulator.simulator.config.PaymentSimulatorProperties;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PaymentSimulatorController implements PaymentSimulatorApi {

    private final Map<String, ProviderResult> states = new ConcurrentHashMap<>();
    private final PaymentSimulatorProperties properties;

    @Override
    public ProviderResult create(String provider, ProviderCommand request) {
        String status =
                request.paymentKey().toLowerCase().contains("unknown") ? "UNKNOWN" : "SUCCESS";
        String providerReference =
                status.equals("SUCCESS")
                        ? provider.toUpperCase() + "-TX-" + request.paymentKey()
                        : null;
        String redirectUrl = properties.getResultUrl() + "?paymentKey=" + request.paymentKey();
        ProviderResult result = new ProviderResult(status, providerReference, redirectUrl);
        states.put(provider + ":" + request.paymentKey(), result);
        return result;
    }

    @Override
    public ProviderResult query(String provider, String key) {
        return states.getOrDefault(provider + ":" + key, new ProviderResult("FAILED", null, null));
    }

    @Override
    public ProviderResult refund(String provider, String key, RefundCommand request) {
        return new ProviderResult(
                "SUCCESS", provider.toUpperCase() + "-REF-" + request.refundKey(), null);
    }
}
