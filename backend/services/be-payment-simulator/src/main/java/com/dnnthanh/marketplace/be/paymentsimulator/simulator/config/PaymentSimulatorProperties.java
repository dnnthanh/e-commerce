package com.dnnthanh.marketplace.be.paymentsimulator.simulator.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties(prefix = "payment-simulator")
@Validated
@Getter
@Setter
public class PaymentSimulatorProperties {

    @NotBlank private String resultUrl;
}
