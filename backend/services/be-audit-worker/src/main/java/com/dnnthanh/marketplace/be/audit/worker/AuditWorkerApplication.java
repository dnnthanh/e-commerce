package com.dnnthanh.marketplace.be.audit.worker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.dnnthanh.marketplace.be")
@EnableScheduling
public class AuditWorkerApplication {

    /**
     * Starts be-audit-worker.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(AuditWorkerApplication.class, args);
    }
}
