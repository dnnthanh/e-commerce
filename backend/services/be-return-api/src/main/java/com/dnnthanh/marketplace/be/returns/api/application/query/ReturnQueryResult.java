package com.dnnthanh.marketplace.be.returns.api.application.query;

import com.dnnthanh.marketplace.be.returns.api.domain.enumtype.ReturnStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Read projection for customer/seller/operations return views. */
public record ReturnQueryResult(
        String returnKey,
        String orderId,
        ReturnStatus status,
        BigDecimal refundableAmount,
        Long receivingWarehouseId,
        LocalDateTime createdAt) {}
