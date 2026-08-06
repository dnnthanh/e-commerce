package com.dnnthanh.marketplace.be.fulfillment.api.api;

import com.dnnthanh.marketplace.be.fulfillment.api.api.request.UpdateShipmentStatusRequest;
import com.dnnthanh.marketplace.be.fulfillment.api.api.response.ShipmentView;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/private/fulfillment")
public interface FulfillmentApi {

    @GetMapping("/orders/{orderId}")
    @PreAuthorize("@authorizationService.hasPermission('FULFILLMENT_VIEW')")
    List<ShipmentView> shipments(@PathVariable String orderId);

    @PutMapping("/shipments/{shipmentNo}/status")
    @PreAuthorize(
            "@authorizationService.hasSellerPermission('FULFILLMENT_VIEW', #request.sellerId())")
    ShipmentView updateStatus(
            @PathVariable String shipmentNo, @RequestBody UpdateShipmentStatusRequest request);
}
