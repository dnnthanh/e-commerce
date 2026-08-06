package com.dnnthanh.marketplace.be.checkout.api.api.request;

import java.util.List;

public record CheckoutRequest(
        String checkoutKey,
        List<CheckoutItemRequest> items,
        List<String> promotionCodes,
        PaymentProviderRequest provider) {}
