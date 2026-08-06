package com.dnnthanh.marketplace.be.review.worker;

/**
 * Enriched delivered order line stored in the Review verified-purchase projection.
 *
 * @param orderLineId authoritative order-line identifier
 * @param productId product identifier resolved from Catalog
 * @param skuId delivered SKU identifier
 */
public record VerifiedPurchaseLine(Long orderLineId, Long productId, Long skuId) {}
