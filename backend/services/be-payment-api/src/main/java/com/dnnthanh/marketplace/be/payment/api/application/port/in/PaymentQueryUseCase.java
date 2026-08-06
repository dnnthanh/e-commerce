package com.dnnthanh.marketplace.be.payment.api.application.port.in;

import com.dnnthanh.marketplace.be.payment.api.application.query.PaymentSearchCriteria;
import com.dnnthanh.marketplace.be.payment.api.application.query.PaymentSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Inbound payment read-model use case. */
public interface PaymentQueryUseCase {
    Page<PaymentSummary> list(PaymentSearchCriteria criteria, Pageable pageable);
}
