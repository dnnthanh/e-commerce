package com.dnnthanh.marketplace.be.order.api.api;

import com.dnnthanh.marketplace.be.order.api.api.request.CancelOrderRequest;
import com.dnnthanh.marketplace.be.order.api.api.request.CreateOrderRequest;
import com.dnnthanh.marketplace.be.order.api.api.request.OrderEventRequest;
import com.dnnthanh.marketplace.be.order.api.api.request.search.OrderSearchRequest;
import com.dnnthanh.marketplace.be.order.api.api.response.InternalOrderResponse;
import com.dnnthanh.marketplace.be.order.api.api.response.OrderLineResponse;
import com.dnnthanh.marketplace.be.order.api.api.response.OrderResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/** Interface-driven HTTP contract for the Order bounded context. */
public interface OrderApi {

    /** Creates an order idempotently from Checkout. */
    @PostMapping("/internal/orders")
    @PreAuthorize("@internalServiceAuthorization.isServiceAccount()")
    OrderResponse create(@Valid @RequestBody CreateOrderRequest request);

    /** Returns a trusted aggregate snapshot. */
    @GetMapping("/internal/orders/{orderNo}")
    @PreAuthorize("@internalServiceAuthorization.isServiceAccount()")
    InternalOrderResponse internalGet(@PathVariable String orderNo);

    /** Returns immutable line snapshots used by Fulfillment and Return. */
    @GetMapping("/internal/orders/{orderNo}/lines")
    @PreAuthorize("@internalServiceAuthorization.isServiceAccount()")
    List<OrderLineResponse> lines(@PathVariable String orderNo);

    /** Applies a payment-success event idempotently. */
    @PostMapping("/internal/orders/{orderNo}/payment-succeeded")
    @PreAuthorize("@internalServiceAuthorization.isServiceAccount()")
    InternalOrderResponse markPaid(
            @PathVariable String orderNo, @Valid @RequestBody OrderEventRequest request);

    /** Applies a fulfillment-start event idempotently. */
    @PostMapping("/internal/orders/{orderNo}/fulfilling")
    @PreAuthorize("@internalServiceAuthorization.isServiceAccount()")
    InternalOrderResponse markFulfilling(
            @PathVariable String orderNo, @Valid @RequestBody OrderEventRequest request);

    /** Applies a fulfillment-complete event idempotently. */
    @PostMapping("/internal/orders/{orderNo}/completed")
    @PreAuthorize("@internalServiceAuthorization.isServiceAccount()")
    InternalOrderResponse markCompleted(
            @PathVariable String orderNo, @Valid @RequestBody OrderEventRequest request);

    /** Returns one order owned by the authenticated customer. */
    @GetMapping("/private/orders/{orderNo}")
    @PreAuthorize("@authorizationService.hasPermission('ORDER_VIEW')")
    OrderResponse get(@PathVariable String orderNo);

    /** Searches the authenticated customer's orders using pageable criteria. */
    @GetMapping("/private/orders")
    @PreAuthorize("@authorizationService.hasPermission('ORDER_VIEW')")
    Page<OrderResponse> search(
            @Valid @ModelAttribute OrderSearchRequest request, Pageable pageable);

    /** Cancels an owned order while cancellation is still allowed. */
    @PostMapping("/private/orders/{orderNo}/cancel")
    @PreAuthorize("@authorizationService.hasPermission('ORDER_CANCEL')")
    OrderResponse cancel(
            @PathVariable String orderNo, @Valid @RequestBody CancelOrderRequest request);
}
