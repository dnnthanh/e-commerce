package com.dnnthanh.marketplace.be.checkout.api.application.port.out;

import com.dnnthanh.marketplace.be.checkout.api.domain.model.CheckoutSaga;
import java.util.Optional;

/** Persistent process-manager boundary for Checkout Saga state and recovery snapshots. */
public interface CheckoutSagaRepositoryPort {

    /**
     * Finds the current Saga state.
     *
     * @param checkoutKey checkout idempotency key
     * @return current Saga when it exists
     */
    Optional<CheckoutSaga> find(String checkoutKey);

    /**
     * Reads the original immutable Checkout command snapshot for recovery.
     *
     * @param checkoutKey checkout idempotency key
     * @return serialized application command snapshot
     */
    Optional<String> findRequestSnapshot(String checkoutKey);

    /**
     * Saves Saga state together with the original command snapshot.
     *
     * @param saga current Saga
     * @param requestJson original serialized Checkout command
     * @return persisted Saga
     */
    CheckoutSaga save(CheckoutSaga saga, String requestJson);
}
