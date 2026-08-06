package com.dnnthanh.marketplace.be.settlement.api.application.dto;

import java.math.BigDecimal;

/** Ledger-vs-stored settlement balance reconciliation result. */
public record SettlementRebuildResult(
        String settlementNo,
        BigDecimal storedPayable,
        BigDecimal ledgerPayable,
        BigDecimal drift) {}
