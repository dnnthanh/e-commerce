package com.dnnthanh.marketplace.be.returns.api.api.response;

import com.dnnthanh.marketplace.be.returns.api.domain.enumtype.ReturnStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReturnView(
        String returnKey,
        String orderId,
        ReturnStatus status,
        BigDecimal refundableAmount,
        Long receivingWarehouseId,
        LocalDateTime createdAt) {}
