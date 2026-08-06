package com.dnnthanh.marketplace.be.inventory.api.application.port.out;

import com.dnnthanh.marketplace.be.inventory.api.application.query.OrderReservationQueryResult;
import java.util.List;

/** Persistence boundary for reservation lifecycle operations requiring row-level atomicity. */
public interface ReservationLifecyclePort {

    /**
     * Associates checkout reservations with the final marketplace order.
     *
     * @param reservationKeys reservation keys
     * @param orderId final order identifier
     */
    void attachOrder(List<String> reservationKeys, String orderId);

    /**
     * Releases reservations idempotently and repairs reserved counters atomically.
     *
     * @param reservationKeys reservation keys
     */
    void releaseReservations(List<String> reservationKeys);

    /**
     * Returns reservations assigned to an order.
     *
     * @param orderId order identifier
     * @return reservation placement projections
     */
    List<OrderReservationQueryResult> findByOrder(String orderId);
}
