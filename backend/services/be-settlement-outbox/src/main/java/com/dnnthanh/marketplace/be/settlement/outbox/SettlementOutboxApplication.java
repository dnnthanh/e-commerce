package com.dnnthanh.marketplace.be.settlement.outbox;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.dnnthanh.marketplace.be")
@EnableScheduling
public class SettlementOutboxApplication {

    /**
     * Starts be-settlement-outbox.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(SettlementOutboxApplication.class, args);
    }
}
