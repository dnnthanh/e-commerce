package com.dnnthanh.marketplace.be.order.api.adapter.out.persistence;

import com.dnnthanh.marketplace.be.order.api.adapter.out.persistence.entity.OrderInboxJpaEntity;
import com.dnnthanh.marketplace.be.order.api.adapter.out.persistence.entity.OrderJpaEntity;
import com.dnnthanh.marketplace.be.order.api.adapter.out.persistence.entity.OrderOutboxJpaEntity;
import com.dnnthanh.marketplace.be.order.api.adapter.out.persistence.exception.OrderPersistenceException;
import com.dnnthanh.marketplace.be.order.api.adapter.out.persistence.mapper.OrderPersistenceMapper;
import com.dnnthanh.marketplace.be.order.api.adapter.out.persistence.repository.OrderInboxJpaRepository;
import com.dnnthanh.marketplace.be.order.api.adapter.out.persistence.repository.OrderJpaRepository;
import com.dnnthanh.marketplace.be.order.api.adapter.out.persistence.repository.OrderOutboxJpaRepository;
import com.dnnthanh.marketplace.be.order.api.application.port.out.OrderRepositoryPort;
import com.dnnthanh.marketplace.be.order.api.application.query.OrderSearchCriteria;
import com.dnnthanh.marketplace.be.order.api.domain.enumtype.OrderEventType;
import com.dnnthanh.marketplace.be.order.api.domain.model.MarketplaceOrder;
import com.dnnthanh.marketplace.be.platform.stereotype.Persistence;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

/**
 * JPA persistence adapter for ordinary Order CRUD plus transactional inbox/outbox. Relational
 * search uses explicit native SQL for deterministic filtering/pagination and then fetches the
 * aggregate graph in one JPA query to avoid N+1.
 */
@Persistence
@RequiredArgsConstructor
public class OrderPersistenceAdapter implements OrderRepositoryPort {

    private final OrderJpaRepository orderRepository;

    private final OrderOutboxJpaRepository outboxRepository;

    private final OrderInboxJpaRepository inboxRepository;

    private final OrderPersistenceMapper mapper;

    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public Optional<MarketplaceOrder> findByOrderNo(String orderNo) {
        return orderRepository.findByOrderNo(orderNo).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MarketplaceOrder> findByCheckoutKey(String checkoutKey) {
        return orderRepository.findByCheckoutKey(checkoutKey).map(mapper::toDomain);
    }

    @Override
    @Transactional
    public MarketplaceOrder save(MarketplaceOrder order, OrderEventType eventType) {
        OrderJpaEntity entity =
                orderRepository
                        .findByOrderNo(order.orderNo())
                        .orElseGet(() -> mapper.toNewEntity(order));
        mapper.synchronize(order, entity);
        OrderJpaEntity saved = orderRepository.save(entity);
        outboxRepository.save(
                new OrderOutboxJpaEntity(
                        UUID.randomUUID().toString(),
                        order.orderNo(),
                        eventType.name(),
                        serializePayload(order),
                        "PENDING",
                        LocalDateTime.now()));
        return mapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MarketplaceOrder> search(OrderSearchCriteria criteria, Pageable pageable) {
        Page<Long> idPage = orderRepository.searchIdsNative(criteria, pageable);
        if (idPage.isEmpty()) {
            return Page.empty(pageable);
        }
        Map<Long, OrderJpaEntity> byId =
                orderRepository.findAggregateGraphByIdIn(idPage.getContent()).stream()
                        .collect(
                                java.util.stream.Collectors.toMap(
                                        OrderJpaEntity::getId, entity -> entity));
        List<MarketplaceOrder> content =
                idPage.getContent().stream()
                        .map(byId::get)
                        .filter(java.util.Objects::nonNull)
                        .map(mapper::toDomain)
                        .toList();
        return new PageImpl<>(content, pageable, idPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MarketplaceOrder> findUnpaidBefore(LocalDateTime cutoff, int limit) {
        int boundedLimit = Math.max(1, Math.min(limit, 500));
        return orderRepository
                .findExpirableNative(
                        Set.of(
                                com.dnnthanh.marketplace.be.order.api.domain.enumtype.OrderStatus
                                        .CREATED
                                        .name(),
                                com.dnnthanh.marketplace.be.order.api.domain.enumtype.OrderStatus
                                        .PAYMENT_PENDING
                                        .name()),
                        cutoff,
                        PageRequest.of(0, boundedLimit))
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public boolean claimInboxEvent(String consumerName, String eventId) {
        if (inboxRepository.existsByConsumerNameAndEventId(consumerName, eventId)) {
            return false;
        }
        try {
            inboxRepository.saveAndFlush(
                    new OrderInboxJpaEntity(consumerName, eventId, LocalDateTime.now()));
            return true;
        } catch (DataIntegrityViolationException duplicate) {
            return false;
        }
    }

    private String serializePayload(MarketplaceOrder order) {
        try {
            return objectMapper.writeValueAsString(
                    Map.of(
                            "orderNo", order.orderNo(),
                            "userId", order.userId(),
                            "status", order.status().name(),
                            "payableAmount", order.payableAmount()));
        } catch (Exception exception) {
            throw new OrderPersistenceException(
                    "Unable to serialize Order outbox payload", exception);
        }
    }
}
