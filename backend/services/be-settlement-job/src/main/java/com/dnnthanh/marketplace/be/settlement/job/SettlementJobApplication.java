package com.dnnthanh.marketplace.be.settlement.job;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.dnnthanh.marketplace.be")
@EnableScheduling
public class SettlementJobApplication {

    /**
     * Starts be-settlement-job.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(SettlementJobApplication.class, args);
    }
}
