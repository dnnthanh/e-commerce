package com.dnnthanh.marketplace.be.cart.api.application.service;

import com.dnnthanh.marketplace.be.cart.api.application.command.MergeCartCommand;
import com.dnnthanh.marketplace.be.cart.api.application.command.PutCartItemCommand;
import com.dnnthanh.marketplace.be.cart.api.application.port.in.CartUseCase;
import com.dnnthanh.marketplace.be.cart.api.domain.model.ShoppingCart;
import com.dnnthanh.marketplace.be.cart.api.domain.model.ShoppingCart.CartLine;
import com.dnnthanh.marketplace.be.platform.context.UserContext;
import com.dnnthanh.marketplace.be.platform.stereotype.UseCase;
import lombok.RequiredArgsConstructor;

/** HTTP-independent cart orchestration over the canonical versioned aggregate. */
@UseCase
@RequiredArgsConstructor
public class CartServiceImplement implements CartUseCase {
    private static final int MAX_MERGED_QUANTITY = 99;

    private final CartMutationService mutations;
    private final UserContext user;

    @Override
    public ShoppingCart guest(String key) {
        return mutations.getOrCreate(key, null);
    }

    @Override
    public ShoppingCart guestPut(String key, PutCartItemCommand command) {
        mutations.getOrCreate(key, null);
        return mutations.upsert(key, line(command), command.expectedVersion());
    }

    @Override
    public ShoppingCart get() {
        return mutations.getOrCreate(userKey(), user.userId());
    }

    @Override
    public ShoppingCart put(PutCartItemCommand command) {
        mutations.getOrCreate(userKey(), user.userId());
        return mutations.upsert(userKey(), line(command), command.expectedVersion());
    }

    @Override
    public ShoppingCart remove(Long sellerId, Long skuId, long expectedVersion) {
        return mutations.remove(userKey(), sellerId, String.valueOf(skuId), expectedVersion);
    }

    @Override
    public ShoppingCart saveForLater(Long sellerId, Long skuId, long expectedVersion) {
        return mutations.saveForLater(userKey(), sellerId, String.valueOf(skuId), expectedVersion);
    }

    @Override
    public ShoppingCart moveToCart(Long sellerId, Long skuId, long expectedVersion) {
        return mutations.moveToCart(userKey(), sellerId, String.valueOf(skuId), expectedVersion);
    }

    @Override
    public ShoppingCart merge(MergeCartCommand command) {
        mutations.getOrCreate(userKey(), user.userId());
        return mutations.mergeGuest(
                userKey(), command.guestCartKey(), command.expectedVersion(), MAX_MERGED_QUANTITY);
    }

    private String userKey() {
        return "USER-" + user.userId();
    }

    private static CartLine line(PutCartItemCommand command) {
        return new CartLine(
                command.sellerId(),
                String.valueOf(command.skuId()),
                command.quantity(),
                command.priceSnapshot(),
                command.selected());
    }
}
