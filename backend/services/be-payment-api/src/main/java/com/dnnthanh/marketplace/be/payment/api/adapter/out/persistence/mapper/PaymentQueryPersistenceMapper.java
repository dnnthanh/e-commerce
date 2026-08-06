package com.dnnthanh.marketplace.be.payment.api.adapter.out.persistence.mapper;

import com.dnnthanh.marketplace.be.payment.api.adapter.out.persistence.projection.PaymentSearchProjection;
import com.dnnthanh.marketplace.be.payment.api.application.query.PaymentQueryResult;
import com.dnnthanh.marketplace.be.platform.mapping.MapperContract;
import com.dnnthanh.marketplace.be.platform.mapping.PlatformMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = PlatformMapperConfig.class)
public interface PaymentQueryPersistenceMapper extends MapperContract {
    PaymentQueryResult toResult(PaymentSearchProjection projection);
}
