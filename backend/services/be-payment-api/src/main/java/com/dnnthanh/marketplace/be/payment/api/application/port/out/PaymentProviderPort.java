package com.dnnthanh.marketplace.be.payment.api.application.port.out;

import com.dnnthanh.marketplace.be.payment.api.domain.model.Payment;
import com.dnnthanh.marketplace.be.platform.model.CodeEnum;
import java.math.BigDecimal;

/** Anti-corruption port for MoMo/VNPAY/VietQR. */
public interface PaymentProviderPort {

    /**
     * @return provider handled by adapter
     */
    Payment.Provider provider();

    ProviderResult create(Payment payment);

    /** Queries provider state after UNKNOWN. @param payment payment @return provider result */
    ProviderResult query(Payment payment);

    /**
     * Refunds a specific amount idempotently. @param payment payment @param refundKey refund
     * idempotency key @param amount refund amount @return provider result
     */
    ProviderResult refund(Payment payment, String refundKey, BigDecimal amount);

    /**
     * @param status normalized provider status @param transactionId provider transaction id @param
     *     redirectUrl redirect/QR URL
     */
    record ProviderResult(Status status, String transactionId, String redirectUrl) {
        /** Result status. */
        public enum Status implements CodeEnum {
            SUCCESS,
            PENDING,
            FAILED,
            UNKNOWN
        }
    }
}
