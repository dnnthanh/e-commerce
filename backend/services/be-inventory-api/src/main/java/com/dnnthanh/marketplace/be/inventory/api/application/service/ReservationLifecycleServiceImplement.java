package com.dnnthanh.marketplace.be.inventory.api.application.service;

import com.dnnthanh.marketplace.be.inventory.api.application.port.in.ReservationLifecycleUseCase;
import com.dnnthanh.marketplace.be.inventory.api.application.port.out.ReservationLifecyclePort;
import com.dnnthanh.marketplace.be.inventory.api.application.query.OrderReservationQueryResult;
import com.dnnthanh.marketplace.be.platform.stereotype.UseCase;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

/** Reservation lifecycle commands executed after initial stock reservation. */
@UseCase
@RequiredArgsConstructor
public class ReservationLifecycleServiceImplement implements ReservationLifecycleUseCase {

    private final ReservationLifecyclePort reservationLifecyclePort;

    /** Attaches final parent order to a known reservation-key set. */
    @Transactional
    public void attach(List<String> reservationKeys, String orderId) {
        if (reservationKeys == null || reservationKeys.isEmpty()) {
            return;
        }
        reservationLifecyclePort.attachOrder(reservationKeys, orderId);
    }

    /** Releases a bounded reservation set idempotently. */
    @Transactional
    public void release(List<String> reservationKeys) {
        if (reservationKeys == null || reservationKeys.isEmpty()) {
            return;
        }
        reservationLifecyclePort.releaseReservations(reservationKeys);
    }

    /** Reads placement snapshot for fulfillment. */
    @Transactional(readOnly = true)
    public List<OrderReservationQueryResult> byOrder(String orderId) {
        return reservationLifecyclePort.findByOrder(orderId);
    }
}
