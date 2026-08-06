package com.dnnthanh.marketplace.be.order.api.domain.exception;

import com.dnnthanh.marketplace.be.order.api.domain.enumtype.OrderStatus;

/** Raised when a requested transition is not valid from the current order state. */
public class InvalidOrderStateException extends RuntimeException {
    public InvalidOrderStateException(OrderStatus currentStatus, OrderStatus targetStatus) {
        super("Order cannot transition from %s to %s".formatted(currentStatus, targetStatus));
    }
}
