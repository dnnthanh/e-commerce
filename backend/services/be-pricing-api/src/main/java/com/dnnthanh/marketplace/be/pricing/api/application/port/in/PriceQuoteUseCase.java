package com.dnnthanh.marketplace.be.pricing.api.application.port.in;

import com.dnnthanh.marketplace.be.pricing.api.application.query.PriceQuote;
import com.dnnthanh.marketplace.be.pricing.api.application.query.PriceQuoteQuery;

/** Inbound immutable price-quote use case. */
public interface PriceQuoteUseCase {
    PriceQuote quote(PriceQuoteQuery query);
}
