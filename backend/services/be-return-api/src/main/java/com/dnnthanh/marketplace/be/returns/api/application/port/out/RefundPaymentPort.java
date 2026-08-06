package com.dnnthanh.marketplace.be.returns.api.application.port.out;

import com.dnnthanh.marketplace.be.platform.model.CodeEnum;
import java.math.BigDecimal;

/** Anti-corruption port to Payment's idempotent refund command. */
public interface RefundPaymentPort {
    RefundResult refund(String refundKey, String orderId, String returnKey, BigDecimal amount);

    record RefundResult(RefundStatus status) {}

    enum RefundStatus implements CodeEnum {
        SUCCEEDED,
        FAILED,
        UNKNOWN
    }
}
