package com.dnnthanh.marketplace.be.payment.api.api;

import com.dnnthanh.marketplace.be.payment.api.api.request.InternalPaymentRequest;
import com.dnnthanh.marketplace.be.payment.api.api.request.InternalRefundRequest;
import com.dnnthanh.marketplace.be.payment.api.api.response.PaymentResponse;
import com.dnnthanh.marketplace.be.payment.api.api.response.RefundResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/internal/payments")
public interface PaymentInternalApi {

    @PostMapping
    @PreAuthorize("@internalServiceAuthorization.isServiceAccount()")
    PaymentResponse createInternal(@RequestBody InternalPaymentRequest request);

    @PostMapping("/refunds")
    @PreAuthorize("@internalServiceAuthorization.isServiceAccount()")
    RefundResponse refund(@RequestBody InternalRefundRequest request);

    @PostMapping("/{paymentKey}/reconcile")
    @PreAuthorize("@internalServiceAuthorization.isServiceAccount()")
    PaymentResponse reconcile(@PathVariable String paymentKey);
}
