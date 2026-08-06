package com.dnnthanh.marketplace.be.audit.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Boot entry point for be-audit-api. */
@SpringBootApplication(scanBasePackages = "com.dnnthanh.marketplace.be")
@EnableScheduling
public class AuditApiApplication {

    /**
     * Starts be-audit-api.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(AuditApiApplication.class, args);
    }
}
