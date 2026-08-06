package com.dnnthanh.marketplace.be.order.api.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Idempotent-consumer receipt for inbound order integration events. */
@Entity
@Table(name = "inbox_event")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderInboxJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "consumer_name", nullable = false, length = 128)
    private String consumerName;

    @Column(name = "event_id", nullable = false, length = 36)
    private String eventId;

    @Column(name = "processed_at", nullable = false)
    private LocalDateTime processedAt;

    public OrderInboxJpaEntity(String consumerName, String eventId, LocalDateTime processedAt) {
        this.consumerName = consumerName;
        this.eventId = eventId;
        this.processedAt = processedAt;
    }
}
