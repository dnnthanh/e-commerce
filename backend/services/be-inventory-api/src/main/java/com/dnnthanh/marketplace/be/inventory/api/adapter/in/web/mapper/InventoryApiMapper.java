package com.dnnthanh.marketplace.be.inventory.api.adapter.in.web.mapper;

import com.dnnthanh.marketplace.be.inventory.api.api.request.search.InventoryBalanceSearchRequest;
import com.dnnthanh.marketplace.be.inventory.api.api.response.AvailabilityView;
import com.dnnthanh.marketplace.be.inventory.api.api.response.BalanceView;
import com.dnnthanh.marketplace.be.inventory.api.api.response.OrderReservationView;
import com.dnnthanh.marketplace.be.inventory.api.api.response.ReservationResponse;
import com.dnnthanh.marketplace.be.inventory.api.application.query.InventoryBalanceQueryResult;
import com.dnnthanh.marketplace.be.inventory.api.application.query.InventoryBalanceSearchCriteria;
import com.dnnthanh.marketplace.be.inventory.api.application.query.OrderReservationQueryResult;
import com.dnnthanh.marketplace.be.inventory.api.domain.model.Reservation;
import com.dnnthanh.marketplace.be.platform.mapping.MapperContract;
import com.dnnthanh.marketplace.be.platform.mapping.PlatformMapperConfig;
import org.mapstruct.Mapper;

/** Maps Inventory application/domain objects to HTTP contracts. */
@Mapper(config = PlatformMapperConfig.class)
public interface InventoryApiMapper extends MapperContract {
    ReservationResponse toResponse(Reservation value);

    InventoryBalanceSearchCriteria toCriteria(InventoryBalanceSearchRequest request);

    BalanceView toView(InventoryBalanceQueryResult result);

    OrderReservationView toView(OrderReservationQueryResult result);

    default AvailabilityView toAvailability(long available) {
        return new AvailabilityView(available);
    }
}
