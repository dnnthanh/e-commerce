package com.dnnthanh.marketplace.be.settlement.api.application.query;

import com.dnnthanh.marketplace.be.settlement.api.domain.enumtype.SettlementStatus;
import java.math.BigDecimal;

/** Seller settlement read-model row. */
public record SettlementQueryResult(
        String settlementNo,
        Long sellerId,
        BigDecimal grossAmount,
        BigDecimal commissionAmount,
        BigDecimal payableAmount,
        SettlementStatus status) {}
