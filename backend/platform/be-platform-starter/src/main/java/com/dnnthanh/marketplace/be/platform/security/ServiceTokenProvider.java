package com.dnnthanh.marketplace.be.platform.security;

import com.dnnthanh.marketplace.be.platform.config.InternalSecurityProperties;
import com.dnnthanh.marketplace.be.platform.exception.ServiceTokenAcquisitionException;
import com.dnnthanh.marketplace.be.platform.stereotype.Adapter;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Adapter
@ConditionalOnProperty(
        prefix = "marketplace.internal-security",
        name = {"token-uri", "client-id", "client-secret"})
@RequiredArgsConstructor
public class ServiceTokenProvider {

    private final RestClient.Builder restClientBuilder;
    private final InternalSecurityProperties properties;
    private volatile String token;
    private volatile Instant expiresAt = Instant.EPOCH;

    public synchronized String token() {
        if (token != null && Instant.now().isBefore(expiresAt.minusSeconds(20))) {
            return token;
        }
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", properties.getClientId());
        form.add("client_secret", properties.getClientSecret());
        TokenResponse response =
                restClientBuilder
                        .clone()
                        .baseUrl(properties.getTokenUri())
                        .build()
                        .post()
                        .body(form)
                        .retrieve()
                        .body(TokenResponse.class);
        if (response == null || response.access_token() == null) {
            throw new ServiceTokenAcquisitionException("Keycloak returned no service access token");
        }
        token = response.access_token();
        expiresAt = Instant.now().plusSeconds(Math.max(response.expires_in(), 30));
        return token;
    }

    private record TokenResponse(String access_token, long expires_in) {}
}
