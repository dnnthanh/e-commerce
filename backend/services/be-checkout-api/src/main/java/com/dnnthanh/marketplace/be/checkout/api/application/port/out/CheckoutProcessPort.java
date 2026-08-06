package com.dnnthanh.marketplace.be.checkout.api.application.port.out;

import com.dnnthanh.marketplace.be.checkout.api.domain.model.CheckoutProcess;
import java.util.Optional;

/** Durable process-manager persistence and idempotency boundary. */
public interface CheckoutProcessPort {
    Optional<CheckoutProcess> findByIdempotencyKey(String idempotencyKey);

    CheckoutProcess save(CheckoutProcess process);
}
