package com.dnnthanh.marketplace.be.returns.api.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties(prefix = "clients")
@Validated
@Getter
@Setter
public class ReturnClientProperties {

    @NotBlank private String order;
    @NotBlank private String payment;
}
