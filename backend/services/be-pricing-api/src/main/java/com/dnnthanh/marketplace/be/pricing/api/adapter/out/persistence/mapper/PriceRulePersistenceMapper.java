package com.dnnthanh.marketplace.be.pricing.api.adapter.out.persistence.mapper;

import com.dnnthanh.marketplace.be.platform.mapping.MapperContract;
import com.dnnthanh.marketplace.be.platform.mapping.PlatformMapperConfig;
import com.dnnthanh.marketplace.be.pricing.api.adapter.out.persistence.entity.PriceRuleJpaEntity;
import com.dnnthanh.marketplace.be.pricing.api.domain.model.PriceRule;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** MapStruct persistence mapper for temporal pricing rules. */
@Mapper(config = PlatformMapperConfig.class)
public interface PriceRulePersistenceMapper extends MapperContract {
    @Mapping(target = "ruleId", expression = "java(String.valueOf(entity.getId()))")
    @Mapping(target = "sku", expression = "java(String.valueOf(entity.getSkuId()))")
    @Mapping(target = "source", source = "priceSource")
    PriceRule toDomain(PriceRuleJpaEntity entity);
}
