package com.dnnthanh.marketplace.be.payment.api.application.port.in;

import com.dnnthanh.marketplace.be.payment.api.application.command.CreatePaymentCommand;
import com.dnnthanh.marketplace.be.payment.api.application.command.InternalCreatePaymentCommand;
import com.dnnthanh.marketplace.be.payment.api.domain.model.Payment;

public interface PaymentUseCase {
    Result createForCurrentUser(CreatePaymentCommand command);

    Result create(InternalCreatePaymentCommand command);

    Result reconcile(String paymentKey);

    record Result(Payment payment, String redirectUrl) {}
}
