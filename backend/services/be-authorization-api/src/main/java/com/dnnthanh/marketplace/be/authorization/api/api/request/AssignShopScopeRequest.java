package com.dnnthanh.marketplace.be.authorization.api.api.request;

/** Shop-scope assignment request. */
public record AssignShopScopeRequest(Long sellerId, Long shopId) {}
