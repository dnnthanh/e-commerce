package com.dnnthanh.marketplace.be.paymentsimulator.simulator.api.request;

import java.math.BigDecimal;

public record ProviderCommand(String paymentKey, String orderId, BigDecimal amount) {}
