package com.dnnthanh.marketplace.be.checkout.api.application.port.out;

import com.dnnthanh.marketplace.be.checkout.api.application.model.PromotionReservation;
import java.math.BigDecimal;
import java.util.List;

public interface PromotionClientPort {
    PromotionReservation reserve(
            String checkoutKey, String customerId, BigDecimal subtotal, List<String> codes);

    void confirm(String checkoutKey, List<String> promotionIds);

    void release(String checkoutKey, List<String> promotionIds);
}
