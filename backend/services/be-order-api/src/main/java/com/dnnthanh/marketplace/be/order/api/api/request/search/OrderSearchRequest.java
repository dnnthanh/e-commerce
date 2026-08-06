package com.dnnthanh.marketplace.be.order.api.api.request.search;

import com.dnnthanh.marketplace.be.order.api.domain.enumtype.OrderStatus;

/** Optional search filters; customer identity is supplied by the authenticated context. */
public record OrderSearchRequest(OrderStatus status) {}
