package com.dnnthanh.marketplace.be.order.api.api.request;

import jakarta.validation.constraints.NotBlank;

/** Idempotent internal event request used by payment/fulfillment adapters. */
public record OrderEventRequest(@NotBlank String eventId) {}
