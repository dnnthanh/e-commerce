package com.dnnthanh.marketplace.be.settlement.api.adapter.out.persistence.projection;

import java.math.BigDecimal;

/** Native SQL projection for seller settlement search. */
public interface SettlementSummaryProjection {
    String getSettlementNo();

    Long getSellerId();

    BigDecimal getGrossAmount();

    BigDecimal getCommissionAmount();

    BigDecimal getPayableAmount();

    String getStatus();
}
