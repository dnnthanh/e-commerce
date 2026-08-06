package com.dnnthanh.marketplace.be.cart.api.application.service;

import com.dnnthanh.marketplace.be.cart.api.application.port.in.CartCheckoutValidationQuery;
import com.dnnthanh.marketplace.be.cart.api.application.port.in.CartCheckoutValidationQuery.ValidationIssue;
import com.dnnthanh.marketplace.be.cart.api.application.port.in.CartCheckoutValidationQuery.ValidationResult;
import com.dnnthanh.marketplace.be.cart.api.application.port.out.CartPersistencePort;
import com.dnnthanh.marketplace.be.cart.api.application.port.out.CatalogSnapshotPort;
import com.dnnthanh.marketplace.be.cart.api.application.port.out.InventoryAvailabilityPort;
import com.dnnthanh.marketplace.be.cart.api.application.port.out.PricingSnapshotPort;
import com.dnnthanh.marketplace.be.cart.api.domain.exception.InvalidCartMutationException;
import com.dnnthanh.marketplace.be.cart.api.domain.model.ShoppingCart;
import com.dnnthanh.marketplace.be.platform.context.UserContext;
import com.dnnthanh.marketplace.be.platform.stereotype.UseCase;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;

/**
 * Revalidates selected cart lines against authoritative services without holding a DB transaction.
 */
@UseCase
@RequiredArgsConstructor
public class CartCheckoutValidationServiceImplement implements CartCheckoutValidationQuery {
    private final CartPersistencePort carts;
    private final CatalogSnapshotPort catalog;
    private final PricingSnapshotPort pricing;
    private final InventoryAvailabilityPort inventory;
    private final UserContext userContext;

    @Override
    public ValidationResult validateCurrentUser(String channel) {
        return validate("USER-" + userContext.userId(), channel);
    }

    @Override
    public ValidationResult validate(String cartId, String channel) {
        ShoppingCart cart =
                carts.findByCartId(cartId)
                        .orElseThrow(
                                () ->
                                        new InvalidCartMutationException(
                                                "Cart not found: " + cartId));
        List<ValidationIssue> issues = new ArrayList<>();
        for (var line : cart.selectedActiveLines()) {
            var catalogSnapshot = catalog.getBySku(line.sku());
            if (!catalogSnapshot.active()) {
                issues.add(new ValidationIssue(line.sku(), "PRODUCT_INACTIVE", null, null));
                continue;
            }
            if (!catalogSnapshot.sellerId().equals(line.sellerId())) {
                issues.add(new ValidationIssue(line.sku(), "SELLER_MISMATCH", null, null));
            }
            if (catalogSnapshot.purchaseLimit() > 0
                    && line.quantity() > catalogSnapshot.purchaseLimit()) {
                issues.add(
                        new ValidationIssue(
                                line.sku(),
                                "PURCHASE_LIMIT",
                                BigDecimal.valueOf(catalogSnapshot.purchaseLimit()),
                                BigDecimal.valueOf(line.quantity())));
            }
            var price = pricing.quote(line.sku(), line.sellerId(), channel);
            if (price.amount().compareTo(line.unitPrice()) != 0) {
                issues.add(
                        new ValidationIssue(
                                line.sku(), "PRICE_CHANGED", line.unitPrice(), price.amount()));
            }
            var stock = inventory.getBySku(line.sku());
            if (stock.available() < line.quantity()) {
                issues.add(
                        new ValidationIssue(
                                line.sku(),
                                "INSUFFICIENT_STOCK",
                                BigDecimal.valueOf(line.quantity()),
                                BigDecimal.valueOf(stock.available())));
            }
        }
        return new ValidationResult(
                cart.cartId(), cart.version(), issues.isEmpty(), List.copyOf(issues));
    }
}
