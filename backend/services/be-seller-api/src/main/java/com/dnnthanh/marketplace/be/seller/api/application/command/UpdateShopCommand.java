package com.dnnthanh.marketplace.be.seller.api.application.command;

/** Application command for updating public shop material information. */
public record UpdateShopCommand(Long sellerId, String name, String description) {}
