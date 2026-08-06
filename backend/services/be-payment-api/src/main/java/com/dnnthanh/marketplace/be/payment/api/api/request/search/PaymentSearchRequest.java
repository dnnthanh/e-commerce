package com.dnnthanh.marketplace.be.payment.api.api.request.search;

import com.dnnthanh.marketplace.be.payment.api.domain.model.Payment;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Grouped finance/operations payment filters. Pagination is supplied by Spring Pageable. */
@Getter
@Setter
@NoArgsConstructor
public class PaymentSearchRequest {
    private Payment.Status status;
}
