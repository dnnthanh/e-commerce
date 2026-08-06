package com.dnnthanh.marketplace.be.paymentsimulator.simulator.api.request;

import java.math.BigDecimal;

public record RefundCommand(String refundKey, BigDecimal amount) {}
