package com.dnnthanh.marketplace.be.fulfillment.api.adapter.in.web.mapper;

import com.dnnthanh.marketplace.be.fulfillment.api.api.request.UpdateShipmentStatusRequest;
import com.dnnthanh.marketplace.be.fulfillment.api.api.response.ShipmentView;
import com.dnnthanh.marketplace.be.fulfillment.api.application.port.in.FulfillmentUseCase.ShipmentResult;
import com.dnnthanh.marketplace.be.fulfillment.api.application.port.in.FulfillmentUseCase.UpdateShipmentCommand;
import com.dnnthanh.marketplace.be.platform.mapping.ModelResponseMapper;
import com.dnnthanh.marketplace.be.platform.mapping.PlatformMapperConfig;
import com.dnnthanh.marketplace.be.platform.mapping.RequestModelMapper;
import org.mapstruct.Mapper;

/** Maps fulfillment HTTP contracts to/from application commands/results. */
@Mapper(config = PlatformMapperConfig.class)
public interface FulfillmentApiMapper
        extends RequestModelMapper<UpdateShipmentStatusRequest, UpdateShipmentCommand>,
                ModelResponseMapper<ShipmentResult, ShipmentView> {
    @Override
    UpdateShipmentCommand requestToModel(UpdateShipmentStatusRequest request);

    @Override
    ShipmentView modelToResponse(ShipmentResult result);
}
