package com.dnnthanh.marketplace.be.cart.api.application.port.in;

import com.dnnthanh.marketplace.be.cart.api.application.command.MergeCartCommand;
import com.dnnthanh.marketplace.be.cart.api.application.command.PutCartItemCommand;
import com.dnnthanh.marketplace.be.cart.api.domain.model.ShoppingCart;

/** Inbound application port for guest/authenticated cart commands and queries. */
public interface CartUseCase {
    ShoppingCart guest(String cartKey);

    ShoppingCart guestPut(String cartKey, PutCartItemCommand command);

    ShoppingCart get();

    ShoppingCart put(PutCartItemCommand command);

    ShoppingCart remove(Long sellerId, Long skuId, long expectedVersion);

    ShoppingCart saveForLater(Long sellerId, Long skuId, long expectedVersion);

    ShoppingCart moveToCart(Long sellerId, Long skuId, long expectedVersion);

    ShoppingCart merge(MergeCartCommand command);
}
