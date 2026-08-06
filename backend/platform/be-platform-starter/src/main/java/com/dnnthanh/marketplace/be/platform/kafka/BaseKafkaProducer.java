package com.dnnthanh.marketplace.be.platform.kafka;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

/**
 * Base producer that keeps KafkaTemplate plumbing and transport logging out of bounded-context
 * code.
 *
 * @param <T> typed message value published by the concrete producer
 */
@Slf4j
public abstract class BaseKafkaProducer<T> {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    protected BaseKafkaProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = Objects.requireNonNull(kafkaTemplate, "kafkaTemplate");
    }

    /**
     * Sends one typed object while preserving the asynchronous acknowledgment for Outbox callers.
     *
     * @param topic Kafka topic
     * @param key Kafka partition key
     * @param value typed message value
     * @return asynchronous Kafka send result
     */
    protected final CompletableFuture<SendResult<String, Object>> send(
            String topic, String key, T value) {
        Objects.requireNonNull(topic, "topic");
        Objects.requireNonNull(value, "value");
        return kafkaTemplate
                .send(topic, key, value)
                .whenComplete(
                        (result, failure) -> {
                            if (failure != null) {
                                log.warn(
                                        "kafka_publish_failed topic={} keyHash={} valueType={} failure={}",
                                        topic,
                                        Objects.hashCode(key),
                                        value.getClass().getSimpleName(),
                                        failure.toString());
                            }
                        });
    }
}
