package com.dnnthanh.marketplace.be.order.api.application.command;

/** Idempotent external event command used for payment and fulfillment state changes. */
public record OrderEventCommand(String eventId, String orderNo) {}
