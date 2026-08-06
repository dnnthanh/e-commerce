package com.dnnthanh.marketplace.be.promotion.api.application.port.in;

import com.dnnthanh.marketplace.be.promotion.api.application.dto.PromotionReservationResult;
import java.math.BigDecimal;
import java.util.List;

/** Checkout-facing input port for race-safe promotion usage reservation. */
public interface PromotionCheckoutReservationUseCase {
    PromotionReservationResult reserve(
            String checkoutKey, String customerId, BigDecimal subtotal, List<String> codes);

    void confirm(String checkoutKey, List<String> promotionIds);

    void release(String checkoutKey, List<String> promotionIds);
}
