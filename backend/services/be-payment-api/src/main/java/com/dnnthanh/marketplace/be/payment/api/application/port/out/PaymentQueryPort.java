package com.dnnthanh.marketplace.be.payment.api.application.port.out;

import com.dnnthanh.marketplace.be.payment.api.application.query.PaymentQueryResult;
import com.dnnthanh.marketplace.be.payment.api.application.query.PaymentSearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Read-side payment persistence boundary. */
public interface PaymentQueryPort {
    Page<PaymentQueryResult> findRecent(PaymentSearchCriteria criteria, Pageable pageable);
}
