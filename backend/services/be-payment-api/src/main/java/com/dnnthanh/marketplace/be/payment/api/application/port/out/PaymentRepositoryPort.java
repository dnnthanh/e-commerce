package com.dnnthanh.marketplace.be.payment.api.application.port.out;

import com.dnnthanh.marketplace.be.payment.api.domain.model.Payment;
import java.util.Optional;

/** Payment aggregate persistence port. */
public interface PaymentRepositoryPort {

    /** Saves payment. */
    Payment save(Payment payment);

    /** Finds by idempotency key. */
    Optional<Payment> findByKey(String key);

    /** Finds by DB id. */
    Optional<Payment> findById(Long id);

    /** Finds payment for an order. */
    Optional<Payment> findByOrder(String orderId);
}
