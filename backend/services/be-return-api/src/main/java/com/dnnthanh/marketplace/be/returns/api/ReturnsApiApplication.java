package com.dnnthanh.marketplace.be.returns.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.dnnthanh.marketplace.be")
@EnableScheduling
public class ReturnsApiApplication {

    /**
     * Starts be-return-api.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(ReturnsApiApplication.class, args);
    }
}
