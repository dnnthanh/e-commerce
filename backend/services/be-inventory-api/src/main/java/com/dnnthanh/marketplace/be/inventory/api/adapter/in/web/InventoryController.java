package com.dnnthanh.marketplace.be.inventory.api.adapter.in.web;

import com.dnnthanh.marketplace.be.inventory.api.adapter.in.web.mapper.InventoryApiMapper;
import com.dnnthanh.marketplace.be.inventory.api.api.InventoryInternalApi;
import com.dnnthanh.marketplace.be.inventory.api.api.InventoryPrivateApi;
import com.dnnthanh.marketplace.be.inventory.api.api.request.AttachOrderRequest;
import com.dnnthanh.marketplace.be.inventory.api.api.request.ReleaseReservationsRequest;
import com.dnnthanh.marketplace.be.inventory.api.api.request.ReserveInventoryRequest;
import com.dnnthanh.marketplace.be.inventory.api.api.request.search.InventoryBalanceSearchRequest;
import com.dnnthanh.marketplace.be.inventory.api.api.response.AvailabilityView;
import com.dnnthanh.marketplace.be.inventory.api.api.response.BalanceView;
import com.dnnthanh.marketplace.be.inventory.api.api.response.OrderReservationView;
import com.dnnthanh.marketplace.be.inventory.api.api.response.ReservationResponse;
import com.dnnthanh.marketplace.be.inventory.api.application.port.in.InventoryQueryUseCase;
import com.dnnthanh.marketplace.be.inventory.api.application.port.in.ReservationLifecycleUseCase;
import com.dnnthanh.marketplace.be.inventory.api.application.port.in.ReserveInventoryUseCase;
import com.dnnthanh.marketplace.be.platform.api.ApiResponse;
import com.dnnthanh.marketplace.be.platform.api.PageMetadata;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class InventoryController implements InventoryPrivateApi, InventoryInternalApi {
    private final ReserveInventoryUseCase reserveInventoryUseCase;
    private final ReservationLifecycleUseCase reservationLifecycleUseCase;
    private final InventoryQueryUseCase inventoryQueryUseCase;
    private final InventoryApiMapper mapper;

    @Override
    public ReservationResponse reserve(ReserveInventoryRequest request) {
        return reserveInternalCommand(request);
    }

    @Override
    public ApiResponse<List<BalanceView>> balances(
            InventoryBalanceSearchRequest request, Pageable pageable) {
        Page<BalanceView> page =
                inventoryQueryUseCase
                        .balances(mapper.toCriteria(request), pageable)
                        .map(mapper::toView);
        return ApiResponse.success(page.getContent(), PageMetadata.from(page));
    }

    @Override
    public ReservationResponse reserveInternal(ReserveInventoryRequest request) {
        return reserveInternalCommand(request);
    }

    @Override
    public void attachOrder(AttachOrderRequest request) {
        reservationLifecycleUseCase.attach(request.reservationKeys(), request.orderId());
    }

    @Override
    public void release(ReleaseReservationsRequest request) {
        reservationLifecycleUseCase.release(request.reservationKeys());
    }

    @Override
    public List<OrderReservationView> orderReservations(String orderId) {
        return reservationLifecycleUseCase.byOrder(orderId).stream().map(mapper::toView).toList();
    }

    @Override
    public AvailabilityView availability(Long skuId) {
        return mapper.toAvailability(inventoryQueryUseCase.available(skuId));
    }

    private ReservationResponse reserveInternalCommand(ReserveInventoryRequest request) {
        return mapper.toResponse(
                reserveInventoryUseCase.reserve(
                        request.reservationKey(),
                        request.skuId(),
                        request.warehouseId(),
                        request.quantity(),
                        request.ttlMinutes()));
    }
}
