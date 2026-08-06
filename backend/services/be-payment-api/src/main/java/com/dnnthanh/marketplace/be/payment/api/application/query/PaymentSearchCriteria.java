package com.dnnthanh.marketplace.be.payment.api.application.query;

import com.dnnthanh.marketplace.be.payment.api.domain.model.Payment;

/** Grouped application filters for native payment search. */
public record PaymentSearchCriteria(Payment.Status status) {
    public String statusValue() {
        return status == null ? null : status.getCode();
    }
}
