package com.dnnthanh.marketplace.be.payment.api.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "providers")
@Validated
public record PaymentProviderProperties(@NotBlank String simulator) {}
