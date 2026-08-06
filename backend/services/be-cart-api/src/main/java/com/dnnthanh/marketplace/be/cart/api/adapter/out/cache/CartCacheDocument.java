package com.dnnthanh.marketplace.be.cart.api.adapter.out.cache;

import com.dnnthanh.marketplace.be.cart.api.domain.model.ShoppingCart;
import com.dnnthanh.marketplace.be.cart.api.domain.model.ShoppingCart.CartLine;
import com.dnnthanh.marketplace.be.cart.api.domain.model.ShoppingCart.ItemState;
import java.math.BigDecimal;
import java.util.List;

/** Explicit Redis DTO so the rich domain aggregate is never serialized directly. */
public record CartCacheDocument(
        String cartId, String ownerId, long version, List<LineDocument> lines) {
    public static CartCacheDocument fromDomain(ShoppingCart cart) {
        return new CartCacheDocument(
                cart.cartId(),
                cart.ownerId(),
                cart.version(),
                cart.lines().stream().map(LineDocument::fromDomain).toList());
    }

    public ShoppingCart toDomain() {
        return ShoppingCart.rehydrate(
                cartId, ownerId, lines.stream().map(LineDocument::toDomain).toList(), version);
    }

    public record LineDocument(
            Long sellerId,
            String sku,
            int quantity,
            BigDecimal unitPrice,
            boolean selected,
            String state) {
        static LineDocument fromDomain(CartLine line) {
            return new LineDocument(
                    line.sellerId(),
                    line.sku(),
                    line.quantity(),
                    line.unitPrice(),
                    line.selected(),
                    line.state().name());
        }

        CartLine toDomain() {
            return new CartLine(
                    sellerId, sku, quantity, unitPrice, selected, ItemState.valueOf(state));
        }
    }
}
