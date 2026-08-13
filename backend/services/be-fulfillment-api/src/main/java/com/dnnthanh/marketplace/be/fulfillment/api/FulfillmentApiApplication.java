package com.dnnthanh.marketplace.be.fulfillment.api;

import java.time.Clock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.dnnthanh.marketplace.be")
@EnableScheduling
public class FulfillmentApiApplication {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    /**
     * Starts be-fulfillment-api.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(FulfillmentApiApplication.class, args);
    }
}
