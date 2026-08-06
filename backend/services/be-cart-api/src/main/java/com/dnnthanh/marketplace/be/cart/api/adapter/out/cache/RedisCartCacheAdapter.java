package com.dnnthanh.marketplace.be.cart.api.adapter.out.cache;

import com.dnnthanh.marketplace.be.cart.api.application.port.out.ShoppingCartCachePort;
import com.dnnthanh.marketplace.be.cart.api.domain.model.ShoppingCart;
import com.dnnthanh.marketplace.be.platform.cache.MarketplaceCacheManager;
import com.dnnthanh.marketplace.be.platform.cache.MarketplaceCacheNames;
import com.dnnthanh.marketplace.be.platform.stereotype.Adapter;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

/** Redis cache-aside adapter for canonical ShoppingCart snapshots. */
@Adapter
@RequiredArgsConstructor
public class RedisCartCacheAdapter implements ShoppingCartCachePort {
    private final MarketplaceCacheManager cacheManager;

    @Override
    public Optional<ShoppingCart> find(String cartId) {
        return cacheManager
                .get(MarketplaceCacheNames.CARTS, cartId, CartCacheDocument.class)
                .map(CartCacheDocument::toDomain);
    }

    @Override
    public void put(ShoppingCart cart) {
        cacheManager.put(
                MarketplaceCacheNames.CARTS, cart.cartId(), CartCacheDocument.fromDomain(cart));
    }

    @Override
    public void evict(String cartId) {
        cacheManager.evict(MarketplaceCacheNames.CARTS, cartId);
    }
}
