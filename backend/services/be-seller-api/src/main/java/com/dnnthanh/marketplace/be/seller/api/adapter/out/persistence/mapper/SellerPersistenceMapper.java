package com.dnnthanh.marketplace.be.seller.api.adapter.out.persistence.mapper;

import com.dnnthanh.marketplace.be.platform.mapping.MapperContract;
import com.dnnthanh.marketplace.be.platform.mapping.PlatformMapperConfig;
import com.dnnthanh.marketplace.be.seller.api.adapter.out.persistence.entity.ShopJpaEntity;
import com.dnnthanh.marketplace.be.seller.api.domain.enumtype.SellerStatus;
import com.dnnthanh.marketplace.be.seller.api.domain.model.Shop;
import org.mapstruct.Mapper;

/** JPA/domain mapping boundary for seller shop data. */
@Mapper(config = PlatformMapperConfig.class)
public interface SellerPersistenceMapper extends MapperContract {
    default Shop toDomain(ShopJpaEntity entity) {
        return new Shop(
                entity.getId(),
                entity.getSellerId(),
                entity.getSlug(),
                entity.getName(),
                entity.getDescription(),
                SellerStatus.valueOf(entity.getStatus()),
                entity.getUpdatedAt());
    }

    default void copy(Shop shop, ShopJpaEntity entity) {
        entity.setSellerId(shop.sellerId());
        entity.setSlug(shop.slug());
        entity.setName(shop.name());
        entity.setDescription(shop.description());
        entity.setStatus(shop.status().getCode());
        entity.setUpdatedAt(shop.updatedAt());
    }
}
