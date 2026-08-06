package com.dnnthanh.marketplace.be.cart.api.application.port.out;

import com.dnnthanh.marketplace.be.cart.api.domain.model.ShoppingCart;
import java.util.Optional;

/** Fail-open cache boundary. MySQL remains the authoritative cart source of truth. */
public interface ShoppingCartCachePort {
    Optional<ShoppingCart> find(String cartId);

    void put(ShoppingCart cart);

    void evict(String cartId);
}
