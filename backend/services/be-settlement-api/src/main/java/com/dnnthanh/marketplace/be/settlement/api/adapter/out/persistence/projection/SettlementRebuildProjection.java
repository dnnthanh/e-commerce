package com.dnnthanh.marketplace.be.settlement.api.adapter.out.persistence.projection;

import java.math.BigDecimal;

/** Native reconciliation projection comparing stored payable with immutable ledger entries. */
public interface SettlementRebuildProjection {
    String getSettlementNo();

    BigDecimal getStoredPayable();

    BigDecimal getLedgerPayable();
}
