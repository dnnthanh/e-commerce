package com.dnnthanh.marketplace.be.payment.api.application.service;

import com.dnnthanh.marketplace.be.payment.api.application.port.in.PaymentQueryUseCase;
import com.dnnthanh.marketplace.be.payment.api.application.port.out.PaymentQueryPort;
import com.dnnthanh.marketplace.be.payment.api.application.query.PaymentSearchCriteria;
import com.dnnthanh.marketplace.be.payment.api.application.query.PaymentSummary;
import com.dnnthanh.marketplace.be.platform.stereotype.UseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@UseCase
@RequiredArgsConstructor
public class PaymentQueryServiceImplement implements PaymentQueryUseCase {
    private final PaymentQueryPort queries;

    @Override
    public Page<PaymentSummary> list(PaymentSearchCriteria criteria, Pageable pageable) {
        return queries.findRecent(criteria, pageable)
                .map(
                        row ->
                                new PaymentSummary(
                                        row.paymentKey(),
                                        row.orderId(),
                                        row.userId(),
                                        row.provider(),
                                        row.amount(),
                                        row.status(),
                                        row.updatedAt()));
    }
}
