package com.dnnthanh.marketplace.be.inventory.api.application.service;

import com.dnnthanh.marketplace.be.inventory.api.application.port.in.ReserveInventoryUseCase;
import com.dnnthanh.marketplace.be.inventory.api.application.port.out.InventoryRepositoryPort;
import com.dnnthanh.marketplace.be.inventory.api.domain.enumtype.ReservationStatus;
import com.dnnthanh.marketplace.be.inventory.api.domain.exception.InsufficientStockException;
import com.dnnthanh.marketplace.be.inventory.api.domain.exception.InvalidInventoryMutationException;
import com.dnnthanh.marketplace.be.inventory.api.domain.exception.ReservationIdempotencyConflictException;
import com.dnnthanh.marketplace.be.inventory.api.domain.model.InventoryBalance;
import com.dnnthanh.marketplace.be.inventory.api.domain.model.Reservation;
import com.dnnthanh.marketplace.be.platform.stereotype.UseCase;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

/** Idempotent inventory reservation use case. */
@UseCase
@RequiredArgsConstructor
public class ReserveInventoryServiceImplement implements ReserveInventoryUseCase {

    private final InventoryRepositoryPort repository;

    /** Reserves stock exactly once for a business idempotency key. */
    @Transactional
    public Reservation reserve(
            String reservationKey, Long skuId, Long warehouseId, long quantity, int ttlMinutes) {
        var existing = repository.findReservation(reservationKey);
        if (existing.isPresent()) {
            Reservation reservation = existing.get();
            if (!reservation.skuId().equals(skuId)
                    || !reservation.warehouseId().equals(warehouseId)
                    || reservation.quantity() != quantity) {
                throw new ReservationIdempotencyConflictException(reservationKey);
            }
            return reservation;
        }

        InventoryBalance balance =
                repository
                        .findBalance(skuId, warehouseId)
                        .orElseThrow(
                                () ->
                                        new InsufficientStockException(
                                                String.valueOf(skuId), warehouseId, quantity, 0));
        balance.reserve(quantity);
        if (!repository.updateBalance(balance)) {
            throw new InvalidInventoryMutationException(
                    "Inventory balance changed concurrently; retry with the same reservation key");
        }

        Reservation reservation =
                new Reservation(
                        reservationKey,
                        skuId,
                        warehouseId,
                        quantity,
                        ReservationStatus.RESERVED,
                        LocalDateTime.now().plusMinutes(ttlMinutes));
        repository.insertReservation(reservation);
        return reservation;
    }
}
