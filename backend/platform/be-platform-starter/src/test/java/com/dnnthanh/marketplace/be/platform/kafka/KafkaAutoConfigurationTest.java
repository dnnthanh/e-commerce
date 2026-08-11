package com.dnnthanh.marketplace.be.platform.kafka;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.boot.kafka.autoconfigure.KafkaAutoConfiguration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.kafka.core.KafkaTemplate;

/** Regression coverage for the KafkaTemplate required by DomainEventProducer. */
class KafkaAutoConfigurationTest {

    @Test
    void springBootKafkaAutoConfigurationProvidesKafkaTemplate() {
        try (var context = new AnnotationConfigApplicationContext()) {
            context.register(KafkaAutoConfiguration.class);
            context.refresh();

            assertNotNull(context.getBean(KafkaTemplate.class));
        }
    }
}
