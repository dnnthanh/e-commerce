package com.dnnthanh.marketplace.be.payment.api.adapter.out.internal.paymentsimulator.rest;

import com.dnnthanh.marketplace.be.payment.api.config.PaymentProviderProperties;
import com.dnnthanh.marketplace.be.payment.api.domain.model.Payment;
import com.dnnthanh.marketplace.be.platform.stereotype.Adapter;
import org.springframework.web.client.RestClient;

/** MoMo sandbox adapter backed by the internal payment simulator service. */
@Adapter
public class MomoPaymentSimulatorRestAdapter extends AbstractPaymentSimulatorRestAdapter {
    public MomoPaymentSimulatorRestAdapter(
            RestClient.Builder builder, PaymentProviderProperties properties) {
        super(builder, properties, Payment.Provider.MOMO);
    }
}
