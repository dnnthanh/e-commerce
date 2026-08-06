package com.dnnthanh.marketplace.be.fulfillment.outbox;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.dnnthanh.marketplace.be")
@EnableScheduling
public class FulfillmentOutboxApplication {

    /**
     * Starts be-fulfillment-outbox.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(FulfillmentOutboxApplication.class, args);
    }
}
