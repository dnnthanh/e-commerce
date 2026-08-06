package com.dnnthanh.marketplace.be.cart.api.api.request;

/** Request used to merge a guest cart. */
public record MergeCartRequest(String guestCartKey, long expectedVersion) {}
