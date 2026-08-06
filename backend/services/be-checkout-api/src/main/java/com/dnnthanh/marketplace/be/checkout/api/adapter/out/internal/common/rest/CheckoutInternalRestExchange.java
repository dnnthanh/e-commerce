package com.dnnthanh.marketplace.be.checkout.api.adapter.out.internal.common.rest;

import com.dnnthanh.marketplace.be.platform.security.ServiceTokenProvider;
import com.dnnthanh.marketplace.be.platform.stereotype.Adapter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.client.RestClient;

/** Shared authenticated REST exchange used by Checkout's target-specific internal adapters. */
@Adapter
@RequiredArgsConstructor
public class CheckoutInternalRestExchange {
    private final ServiceTokenProvider tokens;
    private final CheckoutRemoteCallExecutor resilience;

    public <T> T get(
            String dependencyName,
            RestClient client,
            String uri,
            Class<T> type,
            Object... uriVariables) {
        return resilience.execute(
                dependencyName,
                () ->
                        client.get()
                                .uri(uri, uriVariables)
                                .headers(headers -> headers.setBearerAuth(tokens.token()))
                                .retrieve()
                                .body(type));
    }

    public <T> T post(
            String dependencyName,
            RestClient client,
            String uri,
            Object body,
            Class<T> type,
            Object... uriVariables) {
        return resilience.execute(
                dependencyName,
                () -> {
                    var response =
                            client.post()
                                    .uri(uri, uriVariables)
                                    .headers(headers -> headers.setBearerAuth(tokens.token()))
                                    .body(body)
                                    .retrieve();
                    if (type == Void.class) {
                        response.toBodilessEntity();
                        return null;
                    }
                    return response.body(type);
                });
    }
}
