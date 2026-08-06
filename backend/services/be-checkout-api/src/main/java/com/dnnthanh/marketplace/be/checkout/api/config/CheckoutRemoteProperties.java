package com.dnnthanh.marketplace.be.checkout.api.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/** Strongly typed configuration for Checkout remote bounded-context endpoints. */
@Component
@ConfigurationProperties(prefix = "checkout.remote")
@Validated
@Getter
@Setter
public class CheckoutRemoteProperties {

    @NotBlank private String pricingBaseUrl;

    @NotBlank private String promotionBaseUrl;

    @NotBlank private String inventoryBaseUrl;

    @NotBlank private String orderBaseUrl;

    @NotBlank private String paymentBaseUrl;
}
