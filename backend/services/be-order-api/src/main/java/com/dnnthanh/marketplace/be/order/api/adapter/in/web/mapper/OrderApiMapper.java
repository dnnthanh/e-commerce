package com.dnnthanh.marketplace.be.order.api.adapter.in.web.mapper;

import com.dnnthanh.marketplace.be.order.api.api.request.CreateOrderRequest;
import com.dnnthanh.marketplace.be.order.api.api.response.InternalOrderResponse;
import com.dnnthanh.marketplace.be.order.api.api.response.InternalSellerOrderResponse;
import com.dnnthanh.marketplace.be.order.api.api.response.OrderLineResponse;
import com.dnnthanh.marketplace.be.order.api.api.response.OrderResponse;
import com.dnnthanh.marketplace.be.order.api.api.response.SellerOrderResponse;
import com.dnnthanh.marketplace.be.order.api.application.command.CreateOrderCommand;
import com.dnnthanh.marketplace.be.order.api.domain.model.MarketplaceOrder;
import com.dnnthanh.marketplace.be.platform.mapping.MapperContract;
import com.dnnthanh.marketplace.be.platform.mapping.PlatformMapperConfig;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = PlatformMapperConfig.class)
public interface OrderApiMapper extends MapperContract {

    CreateOrderCommand toCommand(CreateOrderRequest request);

    CreateOrderCommand.Line toCommand(CreateOrderRequest.LineRequest request);

    OrderResponse toResponse(MarketplaceOrder order);

    SellerOrderResponse toResponse(MarketplaceOrder.SellerOrder sellerOrder);

    default InternalOrderResponse toInternalResponse(MarketplaceOrder order) {
        return new InternalOrderResponse(
                order.orderNo(),
                order.checkoutKey(),
                order.userId(),
                order.grossAmount(),
                order.discountAmount(),
                order.payableAmount(),
                order.status(),
                order.cancellationReason(),
                order.sellerOrders().stream().map(this::toInternalSellerOrderResponse).toList(),
                order.createdAt(),
                order.updatedAt(),
                order.version());
    }

    default InternalSellerOrderResponse toInternalSellerOrderResponse(
            MarketplaceOrder.SellerOrder sellerOrder) {
        return new InternalSellerOrderResponse(
                sellerOrder.sellerId(),
                sellerOrder.sellerOrderNo(),
                sellerOrder.grossAmount(),
                sellerOrder.discountAmount(),
                sellerOrder.payableAmount(),
                sellerOrder.status(),
                lineResponses(sellerOrder));
    }

    default List<OrderLineResponse> toLineResponses(MarketplaceOrder order) {
        return order.sellerOrders().stream()
                .flatMap(sellerOrder -> lineResponses(sellerOrder).stream())
                .toList();
    }

    @Mapping(target = "orderLineId", source = "line.id")
    @Mapping(target = "sellerId", source = "sellerId")
    OrderLineResponse toLineResponse(MarketplaceOrder.OrderLine line, Long sellerId);

    default List<OrderLineResponse> lineResponses(MarketplaceOrder.SellerOrder sellerOrder) {
        return sellerOrder.lines().stream()
                .map(line -> toLineResponse(line, sellerOrder.sellerId()))
                .toList();
    }
}
