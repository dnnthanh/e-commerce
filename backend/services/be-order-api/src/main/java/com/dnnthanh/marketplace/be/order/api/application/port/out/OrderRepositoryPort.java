package com.dnnthanh.marketplace.be.order.api.application.port.out;

import com.dnnthanh.marketplace.be.order.api.application.query.OrderSearchCriteria;
import com.dnnthanh.marketplace.be.order.api.domain.enumtype.OrderEventType;
import com.dnnthanh.marketplace.be.order.api.domain.model.MarketplaceOrder;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Persistence boundary for the Order aggregate and inbox/outbox reliability records. */
public interface OrderRepositoryPort {
    Optional<MarketplaceOrder> findByOrderNo(String orderNo);

    Optional<MarketplaceOrder> findByCheckoutKey(String checkoutKey);

    MarketplaceOrder save(MarketplaceOrder order, OrderEventType eventType);

    Page<MarketplaceOrder> search(OrderSearchCriteria criteria, Pageable pageable);

    /** Finds unpaid orders eligible for expiry without loading the entire table. */
    List<MarketplaceOrder> findUnpaidBefore(LocalDateTime cutoff, int limit);

    boolean claimInboxEvent(String consumerName, String eventId);
}
