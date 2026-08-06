package com.dnnthanh.marketplace.be.paymentsimulator.simulator.api.response;

/** Simulated payment provider result. */
public record ProviderResult(String status, String transactionId, String redirectUrl) {}
