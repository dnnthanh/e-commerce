package com.dnnthanh.marketplace.be.platform.outbox;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

/**
 * Central JSON codec used only at durable Outbox persistence boundaries. Business consumers never
 * parse Kafka JSON strings themselves.
 */
@Component
@RequiredArgsConstructor
public class OutboxPayloadCodec {

    private final ObjectMapper objectMapper;

    /**
     * Serializes a typed payload before it is stored in an Outbox JSON column.
     *
     * @param payload typed event payload
     * @return JSON representation suitable for the Outbox persistence column
     */
    public String write(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception failure) {
            throw new IllegalArgumentException("Outbox payload serialization failed", failure);
        }
    }
}
