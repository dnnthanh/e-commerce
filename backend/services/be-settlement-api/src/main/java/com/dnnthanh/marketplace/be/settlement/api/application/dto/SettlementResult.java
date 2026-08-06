package com.dnnthanh.marketplace.be.settlement.api.application.dto;

import com.dnnthanh.marketplace.be.settlement.api.domain.enumtype.SettlementStatus;
import java.math.BigDecimal;

/** Settlement application read model. */
public record SettlementResult(
        String settlementNo,
        Long sellerId,
        BigDecimal gross,
        BigDecimal commission,
        BigDecimal payable,
        SettlementStatus status) {}
