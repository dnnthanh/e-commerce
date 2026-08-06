package com.dnnthanh.marketplace.be.payment.job;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.dnnthanh.marketplace.be")
@EnableScheduling
public class PaymentJobApplication {

    /**
     * Starts be-payment-job.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(PaymentJobApplication.class, args);
    }
}
