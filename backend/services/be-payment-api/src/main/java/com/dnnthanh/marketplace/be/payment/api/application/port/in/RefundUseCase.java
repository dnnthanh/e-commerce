package com.dnnthanh.marketplace.be.payment.api.application.port.in;

import com.dnnthanh.marketplace.be.payment.api.domain.enumtype.RefundStatus;
import com.dnnthanh.marketplace.be.payment.api.domain.model.Payment;
import java.math.BigDecimal;

public interface RefundUseCase {
    RefundResult refund(String refundKey, String orderId, String returnKey, BigDecimal amount);

    record RefundResult(
            String refundKey,
            RefundStatus status,
            BigDecimal amount,
            Payment.Status paymentStatus) {}
}
