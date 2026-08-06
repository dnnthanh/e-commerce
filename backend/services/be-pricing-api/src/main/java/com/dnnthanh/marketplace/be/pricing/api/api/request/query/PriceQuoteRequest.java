package com.dnnthanh.marketplace.be.pricing.api.api.request.query;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Grouped internal price-quote request. */
@Getter
@Setter
@NoArgsConstructor
public class PriceQuoteRequest {
    private String sku;
    private Long sellerId;
    private String channel = "WEB";
}
