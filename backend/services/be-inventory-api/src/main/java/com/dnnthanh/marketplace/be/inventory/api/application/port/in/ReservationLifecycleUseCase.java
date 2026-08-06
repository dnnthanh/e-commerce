package com.dnnthanh.marketplace.be.inventory.api.application.port.in;

import com.dnnthanh.marketplace.be.inventory.api.application.query.OrderReservationQueryResult;
import java.util.List;

public interface ReservationLifecycleUseCase {
    void attach(List<String> reservationKeys, String orderId);

    void release(List<String> reservationKeys);

    List<OrderReservationQueryResult> byOrder(String orderId);
}
