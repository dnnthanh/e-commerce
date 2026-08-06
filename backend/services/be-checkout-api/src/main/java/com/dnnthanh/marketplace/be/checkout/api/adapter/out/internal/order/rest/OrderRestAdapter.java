package com.dnnthanh.marketplace.be.checkout.api.adapter.out.internal.order.rest;

import com.dnnthanh.marketplace.be.checkout.api.adapter.out.internal.common.rest.CheckoutInternalRestExchange;
import com.dnnthanh.marketplace.be.checkout.api.adapter.out.internal.common.rest.exception.CheckoutRemoteDependencyException;
import com.dnnthanh.marketplace.be.checkout.api.adapter.out.internal.common.rest.mapper.CheckoutInternalRestMapper;
import com.dnnthanh.marketplace.be.checkout.api.adapter.out.internal.order.rest.model.OrderCreateRequest;
import com.dnnthanh.marketplace.be.checkout.api.adapter.out.internal.order.rest.model.OrderCreateResponse;
import com.dnnthanh.marketplace.be.checkout.api.application.model.PricedOrderLine;
import com.dnnthanh.marketplace.be.checkout.api.application.port.out.OrderClientPort;
import com.dnnthanh.marketplace.be.checkout.api.config.CheckoutRemoteProperties;
import com.dnnthanh.marketplace.be.checkout.api.config.CheckoutRemoteResilienceNames;
import com.dnnthanh.marketplace.be.platform.stereotype.Adapter;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.client.RestClient;

@Adapter
@RequiredArgsConstructor
public class OrderRestAdapter implements OrderClientPort {
    private final RestClient.Builder restClientBuilder;
    private final CheckoutRemoteProperties properties;
    private final CheckoutInternalRestExchange exchange;
    private final CheckoutInternalRestMapper mapper;

    @Override
    public String create(
            String checkoutKey,
            String userId,
            List<PricedOrderLine> lines,
            BigDecimal gross,
            BigDecimal discount) {
        OrderCreateRequest request =
                mapper.toOrderCreateRequest(checkoutKey, userId, lines, gross, discount);
        OrderCreateResponse response =
                exchange.post(
                        CheckoutRemoteResilienceNames.ORDER,
                        client(),
                        "/internal/orders",
                        request,
                        OrderCreateResponse.class);
        if (response == null || response.orderNo() == null) {
            throw new CheckoutRemoteDependencyException("Order service returned no order number");
        }
        return response.orderNo();
    }

    private RestClient client() {
        return restClientBuilder.clone().baseUrl(properties.getOrderBaseUrl()).build();
    }
}
