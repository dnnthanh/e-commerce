package com.dnnthanh.marketplace.be.order.api.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Order application dependencies that are intentionally explicit and test-replaceable. */
@Configuration
public class OrderConfiguration {

    /** Provides the system UTC clock used by order creation. */
    @Bean
    Clock orderClock() {
        return Clock.systemUTC();
    }
}
