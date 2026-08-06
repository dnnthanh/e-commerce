package com.dnnthanh.marketplace.be.checkout.api.config;

import lombok.experimental.UtilityClass;

/** Resilience4j instance names used by Checkout remote adapters and configuration. */
@UtilityClass
public class CheckoutRemoteResilienceNames {
    public static final String PRICING = "checkoutPricing";
    public static final String PROMOTION = "checkoutPromotion";
    public static final String INVENTORY = "checkoutInventory";
    public static final String ORDER = "checkoutOrder";
    public static final String PAYMENT = "checkoutPayment";
}
