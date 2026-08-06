package com.dnnthanh.marketplace.be.cart.api.application.command;

/** Application command for replay-safe guest-to-account cart merge. */
public record MergeCartCommand(String guestCartKey, long expectedVersion) {}
