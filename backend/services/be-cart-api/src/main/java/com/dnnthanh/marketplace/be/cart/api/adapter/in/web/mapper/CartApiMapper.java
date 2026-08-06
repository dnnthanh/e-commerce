package com.dnnthanh.marketplace.be.cart.api.adapter.in.web.mapper;

import com.dnnthanh.marketplace.be.cart.api.api.request.MergeCartRequest;
import com.dnnthanh.marketplace.be.cart.api.api.request.PutItemRequest;
import com.dnnthanh.marketplace.be.cart.api.api.response.CartItemView;
import com.dnnthanh.marketplace.be.cart.api.api.response.CartView;
import com.dnnthanh.marketplace.be.cart.api.application.command.MergeCartCommand;
import com.dnnthanh.marketplace.be.cart.api.application.command.PutCartItemCommand;
import com.dnnthanh.marketplace.be.cart.api.domain.model.ShoppingCart;
import com.dnnthanh.marketplace.be.cart.api.domain.model.ShoppingCart.CartLine;
import com.dnnthanh.marketplace.be.platform.mapping.MapperContract;
import com.dnnthanh.marketplace.be.platform.mapping.PlatformMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** MapStruct boundary mapper; HTTP transport does not leak into application use cases. */
@Mapper(config = PlatformMapperConfig.class)
public interface CartApiMapper extends MapperContract {

    PutCartItemCommand toCommand(PutItemRequest request);

    MergeCartCommand toCommand(MergeCartRequest request);

    default CartView toView(ShoppingCart cart) {
        return new CartView(
                cart.cartId(), cart.version(), cart.lines().stream().map(this::toView).toList());
    }

    @Mapping(target = "skuId", source = "sku")
    @Mapping(target = "priceSnapshot", source = "unitPrice")
    CartItemView toView(CartLine line);
}
