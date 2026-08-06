package com.dnnthanh.marketplace.be.cart.api.domain.model;

import com.dnnthanh.marketplace.be.cart.api.domain.exception.CartVersionConflictException;
import com.dnnthanh.marketplace.be.cart.api.domain.exception.InvalidCartMutationException;
import com.dnnthanh.marketplace.be.platform.model.CodeEnum;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Versioned multi-seller cart with active/saved-for-later line lifecycle. */
public final class ShoppingCart {
    private final String cartId;
    private final String ownerId;
    private final Map<LineKey, CartLine> lines = new LinkedHashMap<>();
    private long version;

    public ShoppingCart(String cartId, String ownerId) {
        this.cartId = Objects.requireNonNull(cartId);
        this.ownerId = ownerId;
    }

    public static ShoppingCart rehydrate(
            String cartId, String ownerId, List<CartLine> persistedLines, long version) {
        ShoppingCart cart = new ShoppingCart(cartId, ownerId);
        if (persistedLines != null) {
            persistedLines.forEach(
                    line -> cart.lines.put(new LineKey(line.sellerId(), line.sku()), line));
        }
        cart.version = version;
        return cart;
    }

    public void addOrReplace(CartLine line, long expectedVersion) {
        verifyVersion(expectedVersion);
        if (line.quantity() <= 0)
            throw new InvalidCartMutationException("Cart quantity must be positive");
        CartLine normalized =
                line.state() == ItemState.SAVED_FOR_LATER ? line.withSelected(false) : line;
        lines.put(new LineKey(line.sellerId(), line.sku()), normalized);
        version++;
    }

    public void remove(Long sellerId, String sku, long expectedVersion) {
        verifyVersion(expectedVersion);
        lines.remove(new LineKey(sellerId, sku));
        version++;
    }

    public void saveForLater(Long sellerId, String sku, long expectedVersion) {
        verifyVersion(expectedVersion);
        LineKey key = new LineKey(sellerId, sku);
        CartLine line = requireLine(key);
        lines.put(key, line.withState(ItemState.SAVED_FOR_LATER).withSelected(false));
        version++;
    }

    public void moveToCart(Long sellerId, String sku, long expectedVersion) {
        verifyVersion(expectedVersion);
        LineKey key = new LineKey(sellerId, sku);
        CartLine line = requireLine(key);
        lines.put(key, line.withState(ItemState.ACTIVE));
        version++;
    }

    public void select(Long sellerId, String sku, boolean selected, long expectedVersion) {
        verifyVersion(expectedVersion);
        LineKey key = new LineKey(sellerId, sku);
        CartLine line = requireLine(key);
        if (line.state() == ItemState.SAVED_FOR_LATER && selected) {
            throw new InvalidCartMutationException(
                    "Saved-for-later item cannot be selected for checkout");
        }
        lines.put(key, line.withSelected(selected));
        version++;
    }

    /**
     * Guest merge is replay-safe: max quantity wins instead of summing the same guest cart twice.
     */
    public void mergeGuest(ShoppingCart guest, long expectedVersion, int maxQuantity) {
        verifyVersion(expectedVersion);
        for (CartLine incoming : guest.lines()) {
            LineKey key = new LineKey(incoming.sellerId(), incoming.sku());
            CartLine existing = lines.get(key);
            int quantity =
                    Math.min(
                            maxQuantity,
                            Math.max(
                                    incoming.quantity(),
                                    existing == null ? 0 : existing.quantity()));
            lines.put(
                    key,
                    new CartLine(
                            incoming.sellerId(),
                            incoming.sku(),
                            quantity,
                            incoming.unitPrice(),
                            incoming.selected(),
                            incoming.state()));
        }
        version++;
    }

    public List<CartLine> selectedActiveLines() {
        return lines.values().stream()
                .filter(line -> line.state() == ItemState.ACTIVE && line.selected())
                .toList();
    }

    private CartLine requireLine(LineKey key) {
        CartLine line = lines.get(key);
        if (line == null)
            throw new InvalidCartMutationException("Cart line not found: " + key.sku());
        return line;
    }

    private void verifyVersion(long expectedVersion) {
        if (version != expectedVersion)
            throw new CartVersionConflictException(expectedVersion, version);
    }

    public List<CartLine> lines() {
        return List.copyOf(lines.values());
    }

    public long version() {
        return version;
    }

    public String cartId() {
        return cartId;
    }

    public String ownerId() {
        return ownerId;
    }

    public enum ItemState implements CodeEnum {
        ACTIVE,
        SAVED_FOR_LATER
    }

    public record CartLine(
            Long sellerId,
            String sku,
            int quantity,
            BigDecimal unitPrice,
            boolean selected,
            ItemState state) {
        public CartLine {
            Objects.requireNonNull(sellerId);
            Objects.requireNonNull(sku);
            Objects.requireNonNull(unitPrice);
            state = state == null ? ItemState.ACTIVE : state;
        }

        public CartLine(
                Long sellerId, String sku, int quantity, BigDecimal unitPrice, boolean selected) {
            this(sellerId, sku, quantity, unitPrice, selected, ItemState.ACTIVE);
        }

        CartLine withSelected(boolean value) {
            return new CartLine(sellerId, sku, quantity, unitPrice, value, state);
        }

        CartLine withState(ItemState value) {
            return new CartLine(sellerId, sku, quantity, unitPrice, selected, value);
        }
    }

    private record LineKey(Long sellerId, String sku) {}
}
