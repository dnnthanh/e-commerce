package com.dnnthanh.marketplace.be.gateway.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.dnnthanh.marketplace.be")
@EnableScheduling
public class GatewayAppApplication {

    /**
     * Starts be-gateway.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(GatewayAppApplication.class, args);
    }
}
