package com.dnnthanh.marketplace.be.operations.outbox;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.dnnthanh.marketplace.be")
@EnableScheduling
public class OperationsOutboxApplication {

    /**
     * Starts be-operations-outbox.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(OperationsOutboxApplication.class, args);
    }
}
