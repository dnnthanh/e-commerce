package com.dnnthanh.marketplace.be.order.api.adapter.in.web;

import com.dnnthanh.marketplace.be.order.api.adapter.in.web.mapper.OrderApiMapper;
import com.dnnthanh.marketplace.be.order.api.api.OrderApi;
import com.dnnthanh.marketplace.be.order.api.api.request.CancelOrderRequest;
import com.dnnthanh.marketplace.be.order.api.api.request.CreateOrderRequest;
import com.dnnthanh.marketplace.be.order.api.api.request.OrderEventRequest;
import com.dnnthanh.marketplace.be.order.api.api.request.search.OrderSearchRequest;
import com.dnnthanh.marketplace.be.order.api.api.response.InternalOrderResponse;
import com.dnnthanh.marketplace.be.order.api.api.response.OrderLineResponse;
import com.dnnthanh.marketplace.be.order.api.api.response.OrderResponse;
import com.dnnthanh.marketplace.be.order.api.application.command.CancelOrderCommand;
import com.dnnthanh.marketplace.be.order.api.application.command.OrderEventCommand;
import com.dnnthanh.marketplace.be.order.api.application.port.in.OrderCommandUseCase;
import com.dnnthanh.marketplace.be.order.api.application.port.in.OrderQueryUseCase;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OrderController implements OrderApi {

    private final OrderCommandUseCase commandUseCase;

    private final OrderQueryUseCase queryUseCase;

    private final OrderApiMapper mapper;

    @Override
    public OrderResponse create(CreateOrderRequest request) {
        return mapper.toResponse(commandUseCase.create(mapper.toCommand(request)));
    }

    @Override
    public InternalOrderResponse internalGet(String orderNo) {
        return mapper.toInternalResponse(queryUseCase.internalGet(orderNo));
    }

    @Override
    public List<OrderLineResponse> lines(String orderNo) {
        return mapper.toLineResponses(queryUseCase.internalGet(orderNo));
    }

    @Override
    public InternalOrderResponse markPaid(String orderNo, OrderEventRequest request) {
        return mapper.toInternalResponse(
                commandUseCase.markPaid(new OrderEventCommand(request.eventId(), orderNo)));
    }

    @Override
    public InternalOrderResponse markFulfilling(String orderNo, OrderEventRequest request) {
        return mapper.toInternalResponse(
                commandUseCase.markFulfilling(new OrderEventCommand(request.eventId(), orderNo)));
    }

    @Override
    public InternalOrderResponse markCompleted(String orderNo, OrderEventRequest request) {
        return mapper.toInternalResponse(
                commandUseCase.markCompleted(new OrderEventCommand(request.eventId(), orderNo)));
    }

    @Override
    public OrderResponse get(String orderNo) {
        return mapper.toResponse(queryUseCase.getOwned(orderNo));
    }

    @Override
    public Page<OrderResponse> search(OrderSearchRequest request, Pageable pageable) {
        return queryUseCase.search(request.status(), pageable).map(mapper::toResponse);
    }

    @Override
    public OrderResponse cancel(String orderNo, CancelOrderRequest request) {
        return mapper.toResponse(
                commandUseCase.cancel(new CancelOrderCommand(orderNo, request.reason())));
    }
}
