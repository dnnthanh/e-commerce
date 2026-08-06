package com.dnnthanh.marketplace.be.checkout.api.application.port.out;

import com.dnnthanh.marketplace.be.checkout.api.application.model.PaymentProvider;
import com.dnnthanh.marketplace.be.checkout.api.application.model.PaymentResult;
import java.math.BigDecimal;

public interface PaymentClientPort {
    PaymentResult create(
            String paymentKey,
            String orderNo,
            String userId,
            PaymentProvider provider,
            BigDecimal amount);
}
