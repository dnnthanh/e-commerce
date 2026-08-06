package com.dnnthanh.marketplace.be.checkout.api.adapter.out.internal.inventory.rest;

import com.dnnthanh.marketplace.be.checkout.api.adapter.out.internal.common.rest.CheckoutInternalRestExchange;
import com.dnnthanh.marketplace.be.checkout.api.adapter.out.internal.common.rest.mapper.CheckoutInternalRestMapper;
import com.dnnthanh.marketplace.be.checkout.api.adapter.out.internal.inventory.rest.model.InventoryReservationRequest;
import com.dnnthanh.marketplace.be.checkout.api.application.model.CheckoutItemCommand;
import com.dnnthanh.marketplace.be.checkout.api.application.port.out.InventoryClientPort;
import com.dnnthanh.marketplace.be.checkout.api.config.CheckoutRemoteProperties;
import com.dnnthanh.marketplace.be.checkout.api.config.CheckoutRemoteResilienceNames;
import com.dnnthanh.marketplace.be.platform.stereotype.Adapter;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.client.RestClient;

@Adapter
@RequiredArgsConstructor
public class InventoryRestAdapter implements InventoryClientPort {
    private static final int RESERVATION_TTL_MINUTES = 15;

    private final RestClient.Builder restClientBuilder;
    private final CheckoutRemoteProperties properties;
    private final CheckoutInternalRestExchange exchange;
    private final CheckoutInternalRestMapper mapper;

    @Override
    public List<String> reserve(String checkoutKey, List<CheckoutItemCommand> items) {
        RestClient client = client();
        List<String> keys = new ArrayList<>();
        for (int index = 0; index < items.size(); index++) {
            CheckoutItemCommand item = items.get(index);
            String reservationKey = checkoutKey + "-" + index;
            InventoryReservationRequest request =
                    mapper.toInventoryReservationRequest(
                            reservationKey, item, RESERVATION_TTL_MINUTES);
            exchange.post(
                    CheckoutRemoteResilienceNames.INVENTORY,
                    client,
                    "/internal/inventory/reservations",
                    request,
                    Void.class);
            keys.add(reservationKey);
        }
        return List.copyOf(keys);
    }

    @Override
    public void attachOrder(List<String> keys, String orderNo) {
        exchange.post(
                CheckoutRemoteResilienceNames.INVENTORY,
                client(),
                "/internal/inventory/reservations/attach-order",
                mapper.toInventoryAttachOrderRequest(keys, orderNo),
                Void.class);
    }

    @Override
    public void release(List<String> keys) {
        if (keys.isEmpty()) {
            return;
        }
        exchange.post(
                CheckoutRemoteResilienceNames.INVENTORY,
                client(),
                "/internal/inventory/reservations/release",
                mapper.toInventoryReleaseRequest(keys),
                Void.class);
    }

    private RestClient client() {
        return restClientBuilder.clone().baseUrl(properties.getInventoryBaseUrl()).build();
    }
}
