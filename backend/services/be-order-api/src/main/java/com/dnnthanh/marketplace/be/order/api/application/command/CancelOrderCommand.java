package com.dnnthanh.marketplace.be.order.api.application.command;

import com.dnnthanh.marketplace.be.order.api.domain.enumtype.CancellationReason;

/**
 * Customer order cancellation command; requester identity is resolved by the application service.
 */
public record CancelOrderCommand(String orderNo, CancellationReason reason) {}
