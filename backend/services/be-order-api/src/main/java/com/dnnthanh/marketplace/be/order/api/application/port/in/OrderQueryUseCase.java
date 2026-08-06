package com.dnnthanh.marketplace.be.order.api.application.port.in;

import com.dnnthanh.marketplace.be.order.api.domain.enumtype.OrderStatus;
import com.dnnthanh.marketplace.be.order.api.domain.model.MarketplaceOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderQueryUseCase {
    MarketplaceOrder internalGet(String orderNo);

    MarketplaceOrder getOwned(String orderNo);

    Page<MarketplaceOrder> search(OrderStatus status, Pageable pageable);
}
