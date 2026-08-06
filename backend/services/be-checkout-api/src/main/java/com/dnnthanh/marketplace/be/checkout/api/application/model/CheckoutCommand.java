package com.dnnthanh.marketplace.be.checkout.api.application.model;

import java.util.List;

public record CheckoutCommand(
        String checkoutKey,
        List<CheckoutItemCommand> items,
        List<String> promotionCodes,
        PaymentProvider provider) {

    public CheckoutCommand {
        items = items == null ? List.of() : List.copyOf(items);
        promotionCodes = promotionCodes == null ? List.of() : List.copyOf(promotionCodes);
    }
}
