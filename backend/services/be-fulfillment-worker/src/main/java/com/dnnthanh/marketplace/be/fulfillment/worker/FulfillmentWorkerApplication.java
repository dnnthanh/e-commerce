package com.dnnthanh.marketplace.be.fulfillment.worker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.dnnthanh.marketplace.be")
@EnableScheduling
public class FulfillmentWorkerApplication {

    /**
     * Starts be-fulfillment-worker.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(FulfillmentWorkerApplication.class, args);
    }
}
