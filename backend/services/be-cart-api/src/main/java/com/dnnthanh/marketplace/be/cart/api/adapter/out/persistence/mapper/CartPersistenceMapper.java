package com.dnnthanh.marketplace.be.cart.api.adapter.out.persistence.mapper;

import com.dnnthanh.marketplace.be.cart.api.adapter.out.persistence.entity.CartItemJpaEntity;
import com.dnnthanh.marketplace.be.cart.api.adapter.out.persistence.entity.CartJpaEntity;
import com.dnnthanh.marketplace.be.cart.api.domain.model.ShoppingCart;
import com.dnnthanh.marketplace.be.cart.api.domain.model.ShoppingCart.CartLine;
import com.dnnthanh.marketplace.be.platform.mapping.MapperContract;
import com.dnnthanh.marketplace.be.platform.mapping.PlatformMapperConfig;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** MapStruct persistence mapper for cart line/entity conversion. */
@Mapper(config = PlatformMapperConfig.class)
public interface CartPersistenceMapper extends MapperContract {

    @Mapping(target = "sku", expression = "java(String.valueOf(entity.getSkuId()))")
    @Mapping(target = "unitPrice", source = "priceSnapshot")
    @Mapping(target = "state", source = "itemState")
    CartLine toDomain(CartItemJpaEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cart", ignore = true)
    @Mapping(target = "skuId", expression = "java(Long.valueOf(line.sku()))")
    @Mapping(target = "priceSnapshot", source = "unitPrice")
    @Mapping(target = "itemState", source = "state")
    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
    CartItemJpaEntity toEntity(CartLine line);

    default ShoppingCart toDomain(CartJpaEntity entity) {
        List<CartLine> lines = entity.getItems().stream().map(this::toDomain).toList();
        return ShoppingCart.rehydrate(
                entity.getCartKey(), entity.getUserId(), lines, entity.getVersion());
    }

    default void synchronize(ShoppingCart cart, CartJpaEntity entity) {
        entity.setUserId(cart.ownerId());
        entity.replaceItems(cart.lines().stream().map(this::toEntity).toList());
    }
}
