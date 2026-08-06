package com.dnnthanh.marketplace.be.settlement.api.application.port.out;

import com.dnnthanh.marketplace.be.settlement.api.domain.model.SellerSettlementLedger;

/** Append-only seller settlement ledger persistence boundary. */
public interface SettlementLedgerPort {
    SellerSettlementLedger loadOrCreate(Long sellerId);

    SellerSettlementLedger save(SellerSettlementLedger ledger);

    boolean markSourceEventOnce(String eventId);
}
