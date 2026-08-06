package com.dnnthanh.marketplace.be.pricing.api.api;

import com.dnnthanh.marketplace.be.pricing.api.api.request.query.PriceQueryRequest;
import com.dnnthanh.marketplace.be.pricing.api.api.response.PriceView;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Public effective-price contract. */
public interface PricingApi {
    @GetMapping("/prices")
    PriceView price(@Valid @ModelAttribute PriceQueryRequest request);
}
