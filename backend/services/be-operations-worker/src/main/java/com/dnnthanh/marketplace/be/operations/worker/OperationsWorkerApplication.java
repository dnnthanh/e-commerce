package com.dnnthanh.marketplace.be.operations.worker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.dnnthanh.marketplace.be")
@EnableScheduling
public class OperationsWorkerApplication {

    /**
     * Starts be-operations-worker.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(OperationsWorkerApplication.class, args);
    }
}
