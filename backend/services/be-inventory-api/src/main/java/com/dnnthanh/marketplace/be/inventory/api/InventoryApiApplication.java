package com.dnnthanh.marketplace.be.inventory.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.dnnthanh.marketplace.be")
@EnableScheduling
public class InventoryApiApplication {

    /**
     * Starts be-inventory-api.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(InventoryApiApplication.class, args);
    }
}
