package com.dnnthanh.marketplace.be.inventory.job;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.dnnthanh.marketplace.be")
@EnableScheduling
public class InventoryJobApplication {

    /**
     * Starts be-inventory-job.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(InventoryJobApplication.class, args);
    }
}
