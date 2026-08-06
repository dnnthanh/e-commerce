package com.dnnthanh.marketplace.be.checkout.api.application.service;

import com.dnnthanh.marketplace.be.checkout.api.application.exception.CheckoutSnapshotException;
import com.dnnthanh.marketplace.be.checkout.api.application.model.CheckoutCommand;
import com.dnnthanh.marketplace.be.checkout.api.application.model.CheckoutResult;
import com.dnnthanh.marketplace.be.checkout.api.application.port.in.CheckoutRecoveryUseCase;
import com.dnnthanh.marketplace.be.checkout.api.application.port.in.CheckoutUseCase;
import com.dnnthanh.marketplace.be.checkout.api.application.port.out.CheckoutSagaRepositoryPort;
import com.dnnthanh.marketplace.be.platform.stereotype.UseCase;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

/**
 * Restores a persisted Checkout command and delegates recovery to the idempotent process manager.
 */
@UseCase
@RequiredArgsConstructor
public class CheckoutRecoveryServiceImplement implements CheckoutRecoveryUseCase {

    private final CheckoutSagaRepositoryPort sagaRepository;

    private final CheckoutUseCase orchestrator;

    private final ObjectMapper objectMapper;

    /**
     * Recovers one Saga from its original persisted command snapshot.
     *
     * @param checkoutKey checkout idempotency key
     * @return current recovered checkout result
     */
    @Override
    public CheckoutResult recover(String checkoutKey) {
        String snapshot =
                sagaRepository
                        .findRequestSnapshot(checkoutKey)
                        .orElseThrow(
                                () ->
                                        new CheckoutSnapshotException(
                                                "Checkout snapshot was not found: " + checkoutKey));
        try {
            CheckoutCommand command = objectMapper.readValue(snapshot, CheckoutCommand.class);
            return orchestrator.checkout(command);
        } catch (RuntimeException failure) {
            throw failure;
        } catch (Exception failure) {
            throw new CheckoutSnapshotException(
                    "Checkout snapshot cannot be restored: " + checkoutKey, failure);
        }
    }
}
