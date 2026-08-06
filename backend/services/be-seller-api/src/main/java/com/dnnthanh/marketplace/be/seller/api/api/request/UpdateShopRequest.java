package com.dnnthanh.marketplace.be.seller.api.api.request;

/** Request used to update seller shop details. */
public record UpdateShopRequest(Long sellerId, String name, String description) {}
