package com.dnnthanh.marketplace.be.promotion.api.api;

import com.dnnthanh.marketplace.be.promotion.api.api.request.ReservationMutationRequest;
import com.dnnthanh.marketplace.be.promotion.api.api.request.ReservePromotionRequest;
import com.dnnthanh.marketplace.be.promotion.api.api.response.ReservePromotionResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/** Authenticated Checkout-facing promotion reservation boundary. */
@RequestMapping("/internal/promotions/reservations")
@PreAuthorize("@internalServiceAuthorization.isServiceAccount()")
public interface PromotionInternalApi {

    @PostMapping
    ReservePromotionResponse reserve(@RequestBody ReservePromotionRequest request);

    @PostMapping("/{checkoutKey}/confirm")
    void confirm(@PathVariable String checkoutKey, @RequestBody ReservationMutationRequest request);

    @PostMapping("/{checkoutKey}/release")
    void release(@PathVariable String checkoutKey, @RequestBody ReservationMutationRequest request);
}
