package com.dnnthanh.marketplace.be.catalog.api.application.query;

/** Grouped application criteria for relational Catalog search. */
public record ProductSearchCriteria(String keyword, Long sellerId, Long categoryId) {}
