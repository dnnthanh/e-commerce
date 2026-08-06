package com.dnnthanh.marketplace.be.checkout.job;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.dnnthanh.marketplace.be")
@EnableScheduling
public class CheckoutJobApplication {

    /**
     * Starts be-checkout-job.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(CheckoutJobApplication.class, args);
    }
}
