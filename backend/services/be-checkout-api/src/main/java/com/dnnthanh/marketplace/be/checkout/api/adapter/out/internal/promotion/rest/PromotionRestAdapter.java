package com.dnnthanh.marketplace.be.checkout.api.adapter.out.internal.promotion.rest;

import com.dnnthanh.marketplace.be.checkout.api.adapter.out.internal.common.rest.CheckoutInternalRestExchange;
import com.dnnthanh.marketplace.be.checkout.api.adapter.out.internal.common.rest.exception.CheckoutRemoteDependencyException;
import com.dnnthanh.marketplace.be.checkout.api.adapter.out.internal.common.rest.mapper.CheckoutInternalRestMapper;
import com.dnnthanh.marketplace.be.checkout.api.adapter.out.internal.promotion.rest.model.PromotionReserveRequest;
import com.dnnthanh.marketplace.be.checkout.api.adapter.out.internal.promotion.rest.model.PromotionReserveResponse;
import com.dnnthanh.marketplace.be.checkout.api.application.model.PromotionReservation;
import com.dnnthanh.marketplace.be.checkout.api.application.port.out.PromotionClientPort;
import com.dnnthanh.marketplace.be.checkout.api.config.CheckoutRemoteProperties;
import com.dnnthanh.marketplace.be.checkout.api.config.CheckoutRemoteResilienceNames;
import com.dnnthanh.marketplace.be.platform.stereotype.Adapter;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.client.RestClient;

@Adapter
@RequiredArgsConstructor
public class PromotionRestAdapter implements PromotionClientPort {
    private final RestClient.Builder restClientBuilder;
    private final CheckoutRemoteProperties properties;
    private final CheckoutInternalRestExchange exchange;
    private final CheckoutInternalRestMapper mapper;

    @Override
    public PromotionReservation reserve(
            String checkoutKey, String customerId, BigDecimal subtotal, List<String> codes) {
        PromotionReserveRequest request =
                mapper.toPromotionReserveRequest(
                        checkoutKey, customerId, subtotal, codes == null ? List.of() : codes);
        PromotionReserveResponse response =
                exchange.post(
                        CheckoutRemoteResilienceNames.PROMOTION,
                        client(),
                        "/internal/promotions/reservations",
                        request,
                        PromotionReserveResponse.class);
        if (response == null || response.totalDiscount() == null) {
            throw new CheckoutRemoteDependencyException(
                    "Promotion service returned no reservation result");
        }
        return mapper.toPromotionReservation(response);
    }

    @Override
    public void confirm(String checkoutKey, List<String> promotionIds) {
        exchange.post(
                CheckoutRemoteResilienceNames.PROMOTION,
                client(),
                "/internal/promotions/reservations/{checkoutKey}/confirm",
                mapper.toPromotionReservationMutationRequest(promotionIds),
                Void.class,
                checkoutKey);
    }

    @Override
    public void release(String checkoutKey, List<String> promotionIds) {
        if (promotionIds.isEmpty()) {
            return;
        }
        exchange.post(
                CheckoutRemoteResilienceNames.PROMOTION,
                client(),
                "/internal/promotions/reservations/{checkoutKey}/release",
                mapper.toPromotionReservationMutationRequest(promotionIds),
                Void.class,
                checkoutKey);
    }

    private RestClient client() {
        return restClientBuilder.clone().baseUrl(properties.getPromotionBaseUrl()).build();
    }
}
