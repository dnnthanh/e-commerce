package com.dnnthanh.marketplace.be.checkout.api.api;

import com.dnnthanh.marketplace.be.checkout.api.api.response.CheckoutResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/** Internal Checkout recovery contract for authenticated service accounts. */
@RequestMapping("/internal/checkout")
public interface CheckoutInternalApi {

    /**
     * Resumes one durable Checkout Saga from its persisted request snapshot.
     *
     * @param checkoutKey checkout idempotency key
     * @return recovered checkout state
     */
    @PostMapping("/{checkoutKey}/recover")
    @PreAuthorize("@internalServiceAuthorization.isServiceAccount()")
    CheckoutResponse recover(@PathVariable String checkoutKey);
}
