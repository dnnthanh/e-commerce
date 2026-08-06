package com.dnnthanh.marketplace.be.authorization.api.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties(prefix = "keycloak-admin")
@Validated
@Getter
@Setter
public class KeycloakAdminProperties {

    @NotBlank private String baseUrl;

    @NotBlank private String realm;

    @NotBlank private String clientId;

    @NotBlank private String clientSecret;
}
