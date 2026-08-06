package com.dnnthanh.marketplace.be.order.api.adapter.out.persistence.mapper;

import com.dnnthanh.marketplace.be.order.api.adapter.out.persistence.entity.OrderJpaEntity;
import com.dnnthanh.marketplace.be.order.api.adapter.out.persistence.entity.OrderLineJpaEntity;
import com.dnnthanh.marketplace.be.order.api.adapter.out.persistence.entity.SellerOrderJpaEntity;
import com.dnnthanh.marketplace.be.order.api.domain.model.MarketplaceOrder;
import com.dnnthanh.marketplace.be.platform.mapping.MapperContract;
import com.dnnthanh.marketplace.be.platform.mapping.PlatformMapperConfig;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = PlatformMapperConfig.class)
public abstract class OrderPersistenceMapper implements MapperContract {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sellerOrders", ignore = true)
    protected abstract OrderJpaEntity toEntity(MarketplaceOrder order);

    @Mapping(target = "order", ignore = true)
    @Mapping(target = "lines", ignore = true)
    protected abstract SellerOrderJpaEntity toEntity(MarketplaceOrder.SellerOrder sellerOrder);

    @Mapping(target = "sellerOrder", ignore = true)
    protected abstract OrderLineJpaEntity toEntity(MarketplaceOrder.OrderLine line);

    protected abstract MarketplaceOrder.OrderLine toDomain(OrderLineJpaEntity line);

    public OrderJpaEntity toNewEntity(MarketplaceOrder order) {
        OrderJpaEntity entity = toEntity(order);
        synchronizeRelationships(order, entity);
        return entity;
    }

    public void synchronize(MarketplaceOrder order, OrderJpaEntity entity) {
        entity.synchronize(
                order.orderNo(),
                order.checkoutKey(),
                order.userId(),
                order.grossAmount(),
                order.discountAmount(),
                order.payableAmount(),
                order.status(),
                order.cancellationReason(),
                order.createdAt(),
                order.updatedAt());
        synchronizeRelationships(order, entity);
    }

    public MarketplaceOrder toDomain(OrderJpaEntity entity) {
        List<MarketplaceOrder.SellerOrder> sellerOrders =
                entity.getSellerOrders().stream().map(this::toDomainSellerOrder).toList();
        return MarketplaceOrder.rehydrate(
                entity.getOrderNo(),
                entity.getCheckoutKey(),
                entity.getUserId(),
                entity.getGrossAmount(),
                entity.getDiscountAmount(),
                sellerOrders,
                entity.getStatus(),
                entity.getCancellationReason(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getVersion());
    }

    private void synchronizeRelationships(MarketplaceOrder order, OrderJpaEntity entity) {
        List<SellerOrderJpaEntity> sellerEntities =
                order.sellerOrders().stream()
                        .map(
                                sellerOrder -> {
                                    SellerOrderJpaEntity sellerEntity = toEntity(sellerOrder);
                                    sellerEntity.replaceLines(
                                            sellerOrder.lines().stream()
                                                    .map(this::toEntity)
                                                    .toList());
                                    return sellerEntity;
                                })
                        .toList();
        entity.replaceSellerOrders(sellerEntities);
    }

    private MarketplaceOrder.SellerOrder toDomainSellerOrder(SellerOrderJpaEntity sellerOrder) {
        return new MarketplaceOrder.SellerOrder(
                sellerOrder.getId(),
                sellerOrder.getSellerId(),
                sellerOrder.getSellerOrderNo(),
                sellerOrder.getGrossAmount(),
                sellerOrder.getDiscountAmount(),
                sellerOrder.getPayableAmount(),
                sellerOrder.getStatus(),
                sellerOrder.getLines().stream().map(this::toDomain).toList());
    }
}
