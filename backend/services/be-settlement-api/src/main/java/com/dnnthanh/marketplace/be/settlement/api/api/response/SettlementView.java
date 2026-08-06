package com.dnnthanh.marketplace.be.settlement.api.api.response;

import com.dnnthanh.marketplace.be.settlement.api.domain.enumtype.SettlementStatus;
import java.math.BigDecimal;

public record SettlementView(
        String settlementNo,
        Long sellerId,
        BigDecimal gross,
        BigDecimal commission,
        BigDecimal payable,
        SettlementStatus status) {}
