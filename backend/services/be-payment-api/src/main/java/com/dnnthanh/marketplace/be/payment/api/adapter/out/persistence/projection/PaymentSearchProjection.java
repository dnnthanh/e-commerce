package com.dnnthanh.marketplace.be.payment.api.adapter.out.persistence.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface PaymentSearchProjection {
    String getPaymentKey();

    String getOrderId();

    String getUserId();

    String getProvider();

    BigDecimal getAmount();

    String getStatus();

    LocalDateTime getUpdatedAt();
}
