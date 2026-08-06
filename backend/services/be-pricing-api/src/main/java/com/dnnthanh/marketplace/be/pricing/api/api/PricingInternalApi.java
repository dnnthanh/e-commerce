package com.dnnthanh.marketplace.be.pricing.api.api;

import com.dnnthanh.marketplace.be.pricing.api.api.request.query.PriceQuoteRequest;
import com.dnnthanh.marketplace.be.pricing.api.api.response.PriceQuoteResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

/** Service-account price quote used by Cart/Checkout validation. */
@RequestMapping("/internal/pricing")
@PreAuthorize("@internalServiceAuthorization.isServiceAccount()")
public interface PricingInternalApi {
    @GetMapping("/quote")
    PriceQuoteResponse quote(@Valid @ModelAttribute PriceQuoteRequest request);
}
