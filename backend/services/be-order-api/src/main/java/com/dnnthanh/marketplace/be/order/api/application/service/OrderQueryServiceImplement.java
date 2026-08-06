package com.dnnthanh.marketplace.be.order.api.application.service;

import com.dnnthanh.marketplace.be.order.api.application.exception.OrderNotFoundException;
import com.dnnthanh.marketplace.be.order.api.application.port.in.OrderQueryUseCase;
import com.dnnthanh.marketplace.be.order.api.application.port.out.OrderRepositoryPort;
import com.dnnthanh.marketplace.be.order.api.application.query.OrderSearchCriteria;
import com.dnnthanh.marketplace.be.order.api.domain.enumtype.OrderStatus;
import com.dnnthanh.marketplace.be.order.api.domain.model.MarketplaceOrder;
import com.dnnthanh.marketplace.be.platform.context.UserContext;
import com.dnnthanh.marketplace.be.platform.stereotype.UseCase;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

/** Read-side Order use cases. */
@UseCase
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderQueryServiceImplement implements OrderQueryUseCase {

    private final OrderRepositoryPort orderRepository;
    private final UserContext userContext;

    /** Returns a trusted internal order snapshot. */
    public MarketplaceOrder internalGet(String orderNo) {
        return require(orderNo);
    }

    /** Returns an order only when it belongs to the current customer. */
    public MarketplaceOrder getOwned(String orderNo) {
        MarketplaceOrder order = require(orderNo);
        if (!Objects.equals(order.userId(), userContext.userId())) {
            throw new OrderNotFoundException();
        }
        return order;
    }

    /** Searches with explicit criteria and paging instead of hard-coded TOP/OFFSET values. */
    public Page<MarketplaceOrder> search(OrderStatus status, Pageable pageable) {
        return orderRepository.search(
                new OrderSearchCriteria(userContext.userId(), status), pageable);
    }

    private MarketplaceOrder require(String orderNo) {
        return orderRepository.findByOrderNo(orderNo).orElseThrow(OrderNotFoundException::new);
    }
}
