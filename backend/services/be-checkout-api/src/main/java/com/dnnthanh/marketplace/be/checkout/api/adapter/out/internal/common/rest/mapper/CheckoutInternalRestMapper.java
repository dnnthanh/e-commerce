package com.dnnthanh.marketplace.be.checkout.api.adapter.out.internal.common.rest.mapper;

import com.dnnthanh.marketplace.be.checkout.api.adapter.out.internal.inventory.rest.model.InventoryAttachOrderRequest;
import com.dnnthanh.marketplace.be.checkout.api.adapter.out.internal.inventory.rest.model.InventoryReleaseRequest;
import com.dnnthanh.marketplace.be.checkout.api.adapter.out.internal.inventory.rest.model.InventoryReservationRequest;
import com.dnnthanh.marketplace.be.checkout.api.adapter.out.internal.order.rest.model.OrderCreateRequest;
import com.dnnthanh.marketplace.be.checkout.api.adapter.out.internal.payment.rest.model.PaymentCreateRequest;
import com.dnnthanh.marketplace.be.checkout.api.adapter.out.internal.payment.rest.model.PaymentCreateResponse;
import com.dnnthanh.marketplace.be.checkout.api.adapter.out.internal.promotion.rest.model.PromotionReservationMutationRequest;
import com.dnnthanh.marketplace.be.checkout.api.adapter.out.internal.promotion.rest.model.PromotionReserveRequest;
import com.dnnthanh.marketplace.be.checkout.api.adapter.out.internal.promotion.rest.model.PromotionReserveResponse;
import com.dnnthanh.marketplace.be.checkout.api.application.model.CheckoutItemCommand;
import com.dnnthanh.marketplace.be.checkout.api.application.model.PaymentProvider;
import com.dnnthanh.marketplace.be.checkout.api.application.model.PaymentResult;
import com.dnnthanh.marketplace.be.checkout.api.application.model.PricedOrderLine;
import com.dnnthanh.marketplace.be.checkout.api.application.model.PromotionReservation;
import com.dnnthanh.marketplace.be.platform.mapping.MapperContract;
import com.dnnthanh.marketplace.be.platform.mapping.PlatformMapperConfig;
import java.math.BigDecimal;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = PlatformMapperConfig.class)
public interface CheckoutInternalRestMapper extends MapperContract {

    @Mapping(target = "skuId", source = "item.skuId")
    @Mapping(target = "warehouseId", source = "item.warehouseId")
    @Mapping(target = "quantity", source = "item.quantity")
    InventoryReservationRequest toInventoryReservationRequest(
            String reservationKey, CheckoutItemCommand item, int ttlMinutes);

    InventoryAttachOrderRequest toInventoryAttachOrderRequest(
            List<String> reservationKeys, String orderId);

    default InventoryReleaseRequest toInventoryReleaseRequest(List<String> reservationKeys) {
        return new InventoryReleaseRequest(reservationKeys);
    }

    OrderCreateRequest toOrderCreateRequest(
            String checkoutKey,
            String userId,
            List<PricedOrderLine> lines,
            BigDecimal grossAmount,
            BigDecimal discountAmount);

    PaymentCreateRequest toPaymentCreateRequest(
            String paymentKey,
            String orderId,
            String userId,
            PaymentProvider provider,
            BigDecimal amount);

    PaymentResult toPaymentResult(PaymentCreateResponse response);

    PromotionReserveRequest toPromotionReserveRequest(
            String checkoutKey, String customerId, BigDecimal subtotal, List<String> codes);

    PromotionReservation toPromotionReservation(PromotionReserveResponse response);

    default PromotionReservationMutationRequest toPromotionReservationMutationRequest(
            List<String> promotionIds) {
        return new PromotionReservationMutationRequest(promotionIds);
    }
}
