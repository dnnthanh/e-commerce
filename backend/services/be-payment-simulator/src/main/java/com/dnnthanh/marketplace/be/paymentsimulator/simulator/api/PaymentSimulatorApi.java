package com.dnnthanh.marketplace.be.paymentsimulator.simulator.api;

import com.dnnthanh.marketplace.be.paymentsimulator.simulator.api.request.ProviderCommand;
import com.dnnthanh.marketplace.be.paymentsimulator.simulator.api.request.RefundCommand;
import com.dnnthanh.marketplace.be.paymentsimulator.simulator.api.response.ProviderResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/sandbox/payments")
public interface PaymentSimulatorApi {

    @PostMapping("/{provider}")
    ProviderResult create(@PathVariable String provider, @RequestBody ProviderCommand request);

    @GetMapping("/{provider}/{key}")
    ProviderResult query(@PathVariable String provider, @PathVariable String key);

    @PostMapping("/{provider}/{key}/refunds")
    ProviderResult refund(
            @PathVariable String provider,
            @PathVariable String key,
            @RequestBody RefundCommand request);
}
