package com.dnnthanh.marketplace.be.inventory.api.api;

import com.dnnthanh.marketplace.be.inventory.api.api.request.AttachOrderRequest;
import com.dnnthanh.marketplace.be.inventory.api.api.request.ReleaseReservationsRequest;
import com.dnnthanh.marketplace.be.inventory.api.api.request.ReserveInventoryRequest;
import com.dnnthanh.marketplace.be.inventory.api.api.response.AvailabilityView;
import com.dnnthanh.marketplace.be.inventory.api.api.response.OrderReservationView;
import com.dnnthanh.marketplace.be.inventory.api.api.response.ReservationResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/internal/inventory")
public interface InventoryInternalApi {

    @PostMapping("/reservations")
    @PreAuthorize("@internalServiceAuthorization.isServiceAccount()")
    ReservationResponse reserveInternal(@Valid @RequestBody ReserveInventoryRequest request);

    @PostMapping("/reservations/attach-order")
    @PreAuthorize("@internalServiceAuthorization.isServiceAccount()")
    void attachOrder(@RequestBody AttachOrderRequest request);

    @PostMapping("/reservations/release")
    @PreAuthorize("@internalServiceAuthorization.isServiceAccount()")
    void release(@RequestBody ReleaseReservationsRequest request);

    @GetMapping("/orders/{orderId}/reservations")
    @PreAuthorize("@internalServiceAuthorization.isServiceAccount()")
    List<OrderReservationView> orderReservations(@PathVariable String orderId);

    @GetMapping("/availability/{skuId}")
    @PreAuthorize("@internalServiceAuthorization.isServiceAccount()")
    AvailabilityView availability(@PathVariable Long skuId);
}
