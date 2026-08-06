package com.dnnthanh.marketplace.be.payment.api;

import com.dnnthanh.marketplace.be.payment.api.config.PaymentProviderProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.dnnthanh.marketplace.be")
@EnableScheduling
@EnableConfigurationProperties(PaymentProviderProperties.class)
public class PaymentApiApplication {

    /**
     * Starts be-payment-api.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(PaymentApiApplication.class, args);
    }
}
