package com.dnnthanh.marketplace.be.platform.kafka;

import com.dnnthanh.marketplace.be.platform.event.DomainEvent;
import com.dnnthanh.marketplace.be.platform.stereotype.Adapter;
import java.util.concurrent.CompletableFuture;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

@Adapter
public class DomainEventProducer extends BaseKafkaProducer<DomainEvent> {

    public DomainEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        super(kafkaTemplate);
    }

    public CompletableFuture<SendResult<String, Object>> publish(
            String topic, String key, DomainEvent event) {
        return send(topic, key, event);
    }
}
