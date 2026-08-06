package com.dnnthanh.marketplace.be.fulfillment.api.adapter.in.web;

import com.dnnthanh.marketplace.be.fulfillment.api.adapter.in.web.mapper.FulfillmentApiMapper;
import com.dnnthanh.marketplace.be.fulfillment.api.api.FulfillmentApi;
import com.dnnthanh.marketplace.be.fulfillment.api.api.request.UpdateShipmentStatusRequest;
import com.dnnthanh.marketplace.be.fulfillment.api.api.response.ShipmentView;
import com.dnnthanh.marketplace.be.fulfillment.api.application.port.in.FulfillmentUseCase;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class FulfillmentController implements FulfillmentApi {
    private final FulfillmentUseCase fulfillment;
    private final FulfillmentApiMapper mapper;

    @Override
    public List<ShipmentView> shipments(String orderId) {
        return fulfillment.shipments(orderId).stream().map(mapper::modelToResponse).toList();
    }

    @Override
    public ShipmentView updateStatus(String shipmentNo, UpdateShipmentStatusRequest request) {
        return mapper.modelToResponse(
                fulfillment.update(shipmentNo, mapper.requestToModel(request)));
    }
}
