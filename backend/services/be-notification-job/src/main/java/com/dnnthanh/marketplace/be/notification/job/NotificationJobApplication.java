package com.dnnthanh.marketplace.be.notification.job;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication(scanBasePackages = "com.dnnthanh.marketplace.be")
public class NotificationJobApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationJobApplication.class, args);
    }
}
