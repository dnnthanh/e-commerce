package com.dnnthanh.marketplace.be.catalog.outbox;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.dnnthanh.marketplace.be")
@EnableScheduling
public class CatalogOutboxApplication {

    /**
     * Starts be-catalog-outbox.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(CatalogOutboxApplication.class, args);
    }
}
