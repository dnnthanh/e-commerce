package com.dnnthanh.marketplace.be.payment.api.application.service;

import com.dnnthanh.marketplace.be.payment.api.application.port.out.PaymentProviderPort;
import com.dnnthanh.marketplace.be.payment.api.domain.exception.UnsupportedPaymentProviderException;
import com.dnnthanh.marketplace.be.payment.api.domain.model.Payment;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/** Registry/factory resolving the anti-corruption adapter for a runtime payment provider. */
@Component
public final class PaymentProviderRegistry {
    private final Map<Payment.Provider, PaymentProviderPort> providers;

    public PaymentProviderRegistry(List<PaymentProviderPort> adapters) {
        EnumMap<Payment.Provider, PaymentProviderPort> byProvider =
                new EnumMap<>(Payment.Provider.class);
        adapters.forEach(adapter -> byProvider.put(adapter.provider(), adapter));
        providers = Map.copyOf(byProvider);
    }

    public PaymentProviderPort require(Payment.Provider provider) {
        PaymentProviderPort adapter = providers.get(provider);
        if (adapter == null) {
            throw new UnsupportedPaymentProviderException(
                    "Unsupported payment provider: " + provider);
        }
        return adapter;
    }
}
