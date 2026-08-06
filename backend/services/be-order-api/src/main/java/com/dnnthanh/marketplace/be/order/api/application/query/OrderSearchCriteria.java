package com.dnnthanh.marketplace.be.order.api.application.query;

import com.dnnthanh.marketplace.be.order.api.domain.enumtype.OrderStatus;

/** Optional filters used by customer/admin order search. */
public record OrderSearchCriteria(String userId, OrderStatus status) {
    public String statusValue() {
        return status == null ? null : status.name();
    }
}
