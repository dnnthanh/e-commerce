package com.dnnthanh.marketplace.be.checkout.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.dnnthanh.marketplace.be")
@EnableScheduling
public class CheckoutApiApplication {

    /**
     * Starts be-checkout-api.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(CheckoutApiApplication.class, args);
    }
}
