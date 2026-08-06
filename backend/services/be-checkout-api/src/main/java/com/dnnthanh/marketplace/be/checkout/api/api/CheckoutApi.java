package com.dnnthanh.marketplace.be.checkout.api.api;

import com.dnnthanh.marketplace.be.checkout.api.api.request.CheckoutRequest;
import com.dnnthanh.marketplace.be.checkout.api.api.response.CheckoutResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/private/checkout")
public interface CheckoutApi {

    @PostMapping
    @PreAuthorize("@authorizationService.hasPermission('CHECKOUT_VIEW')")
    CheckoutResponse checkout(@Valid @RequestBody CheckoutRequest request);
}
