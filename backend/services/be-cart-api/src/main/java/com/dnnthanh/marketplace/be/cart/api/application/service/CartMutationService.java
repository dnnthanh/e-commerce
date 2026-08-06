package com.dnnthanh.marketplace.be.cart.api.application.service;

import com.dnnthanh.marketplace.be.cart.api.application.port.out.CartPersistencePort;
import com.dnnthanh.marketplace.be.cart.api.application.port.out.ShoppingCartCachePort;
import com.dnnthanh.marketplace.be.cart.api.domain.exception.InvalidCartMutationException;
import com.dnnthanh.marketplace.be.cart.api.domain.model.ShoppingCart;
import com.dnnthanh.marketplace.be.cart.api.domain.model.ShoppingCart.CartLine;
import com.dnnthanh.marketplace.be.platform.stereotype.UseCase;
import lombok.RequiredArgsConstructor;

/** Version-safe cart mutations with MySQL source-of-truth and Redis cache-aside acceleration. */
@UseCase
@RequiredArgsConstructor
public class CartMutationService {
    private final CartPersistencePort repository;
    private final ShoppingCartCachePort cache;

    public ShoppingCart getOrCreate(String cartId, String ownerId) {
        return cache.find(cartId)
                .orElseGet(
                        () -> {
                            ShoppingCart durable =
                                    repository
                                            .findByCartId(cartId)
                                            .orElseGet(
                                                    () ->
                                                            repository.create(
                                                                    new ShoppingCart(
                                                                            cartId, ownerId)));
                            cache.put(durable);
                            return durable;
                        });
    }

    public ShoppingCart upsert(String cartId, CartLine line, long expectedVersion) {
        ShoppingCart cart = load(cartId);
        cart.addOrReplace(line, expectedVersion);
        return saveAndCache(cart, expectedVersion);
    }

    public ShoppingCart remove(String cartId, Long sellerId, String sku, long expectedVersion) {
        ShoppingCart cart = load(cartId);
        cart.remove(sellerId, sku, expectedVersion);
        return saveAndCache(cart, expectedVersion);
    }

    public ShoppingCart saveForLater(
            String cartId, Long sellerId, String sku, long expectedVersion) {
        ShoppingCart cart = load(cartId);
        cart.saveForLater(sellerId, sku, expectedVersion);
        return saveAndCache(cart, expectedVersion);
    }

    public ShoppingCart moveToCart(String cartId, Long sellerId, String sku, long expectedVersion) {
        ShoppingCart cart = load(cartId);
        cart.moveToCart(sellerId, sku, expectedVersion);
        return saveAndCache(cart, expectedVersion);
    }

    public ShoppingCart mergeGuest(
            String accountCartId, String guestCartId, long expectedVersion, int maxQuantity) {
        ShoppingCart account = load(accountCartId);
        ShoppingCart guest = load(guestCartId);
        account.mergeGuest(guest, expectedVersion, maxQuantity);
        ShoppingCart saved = saveAndCache(account, expectedVersion);
        repository.delete(guestCartId);
        cache.evict(guestCartId);
        return saved;
    }

    private ShoppingCart load(String cartId) {
        return cache.find(cartId)
                .or(() -> repository.findByCartId(cartId))
                .map(
                        cart -> {
                            cache.put(cart);
                            return cart;
                        })
                .orElseThrow(() -> new InvalidCartMutationException("Cart not found: " + cartId));
    }

    private ShoppingCart saveAndCache(ShoppingCart cart, long expectedVersion) {
        try {
            ShoppingCart saved = repository.save(cart, expectedVersion);
            cache.put(saved);
            return saved;
        } catch (RuntimeException failure) {
            cache.evict(cart.cartId());
            throw failure;
        }
    }
}
