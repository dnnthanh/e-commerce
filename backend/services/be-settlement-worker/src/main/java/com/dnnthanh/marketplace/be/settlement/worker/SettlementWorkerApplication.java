package com.dnnthanh.marketplace.be.settlement.worker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.dnnthanh.marketplace.be")
@EnableScheduling
public class SettlementWorkerApplication {

    /**
     * Starts be-settlement-worker.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(SettlementWorkerApplication.class, args);
    }
}
