package com.dnnthanh.marketplace.be.authorization.outbox;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication(scanBasePackages = "com.dnnthanh.marketplace.be")
public class AuthorizationOutboxApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthorizationOutboxApplication.class, args);
    }
}
