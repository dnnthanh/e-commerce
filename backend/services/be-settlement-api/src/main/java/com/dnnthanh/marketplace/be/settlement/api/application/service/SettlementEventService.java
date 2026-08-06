package com.dnnthanh.marketplace.be.settlement.api.application.service;

import com.dnnthanh.marketplace.be.platform.stereotype.UseCase;
import com.dnnthanh.marketplace.be.settlement.api.application.port.out.SettlementLedgerPort;
import com.dnnthanh.marketplace.be.settlement.api.domain.enumtype.LedgerEntryType;
import com.dnnthanh.marketplace.be.settlement.api.domain.model.SellerSettlementLedger;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

/** Idempotently converts integration events into immutable seller payable ledger entries. */
@UseCase
@RequiredArgsConstructor
public class SettlementEventService {
    private final SettlementLedgerPort repository;

    @Transactional
    public boolean apply(
            String eventId,
            Long sellerId,
            LedgerEntryType type,
            BigDecimal amount,
            String reference) {
        if (!repository.markSourceEventOnce(eventId)) {
            return false;
        }
        SellerSettlementLedger ledger = repository.loadOrCreate(sellerId);
        ledger.append(eventId, type, amount, reference);
        repository.save(ledger);
        return true;
    }
}
