package com.dnnthanh.marketplace.be.payment.api.adapter.out.persistence;

import com.dnnthanh.marketplace.be.payment.api.adapter.out.persistence.mapper.PaymentQueryPersistenceMapper;
import com.dnnthanh.marketplace.be.payment.api.adapter.out.persistence.repository.PaymentQueryJpaRepository;
import com.dnnthanh.marketplace.be.payment.api.application.port.out.PaymentQueryPort;
import com.dnnthanh.marketplace.be.payment.api.application.query.PaymentQueryResult;
import com.dnnthanh.marketplace.be.payment.api.application.query.PaymentSearchCriteria;
import com.dnnthanh.marketplace.be.platform.stereotype.Persistence;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** JPA/native-SQL adapter for ordinary payment operations search. */
@Persistence
@RequiredArgsConstructor
public class PaymentQueryPersistenceAdapter implements PaymentQueryPort {
    private final PaymentQueryJpaRepository repository;
    private final PaymentQueryPersistenceMapper mapper;

    @Override
    public Page<PaymentQueryResult> findRecent(PaymentSearchCriteria criteria, Pageable pageable) {
        return repository.search(criteria, pageable).map(mapper::toResult);
    }
}
