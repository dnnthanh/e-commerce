package com.dnnthanh.marketplace.be.checkout.api.application.service;

import com.dnnthanh.marketplace.be.checkout.api.application.port.out.CheckoutProcessPort;
import com.dnnthanh.marketplace.be.checkout.api.domain.model.CheckoutProcess;
import com.dnnthanh.marketplace.be.platform.stereotype.UseCase;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

/**
 * Durable checkout process manager; remote side effects are represented by ports outside DB
 * transactions.
 */
@UseCase
@RequiredArgsConstructor
public class CheckoutProcessService {
    private final CheckoutProcessPort processPort;

    public CheckoutProcess start(String idempotencyKey) {
        return processPort
                .findByIdempotencyKey(idempotencyKey)
                .orElseGet(
                        () ->
                                processPort.save(
                                        new CheckoutProcess(
                                                UUID.randomUUID().toString(), idempotencyKey)));
    }

    public CheckoutProcess.CompensationPlan fail(CheckoutProcess process, String reason) {
        CheckoutProcess.CompensationPlan plan = process.fail(reason);
        processPort.save(process);
        return plan;
    }
}
