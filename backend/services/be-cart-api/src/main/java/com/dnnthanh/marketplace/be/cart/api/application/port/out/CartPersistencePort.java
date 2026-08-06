package com.dnnthanh.marketplace.be.cart.api.application.port.out;

import com.dnnthanh.marketplace.be.cart.api.domain.model.ShoppingCart;
import java.util.Optional;

/** Version-aware cart source-of-truth boundary. */
public interface CartPersistencePort {
    Optional<ShoppingCart> findByCartId(String cartId);

    ShoppingCart create(ShoppingCart cart);

    ShoppingCart save(ShoppingCart cart, long expectedVersion);

    void delete(String cartId);
}
