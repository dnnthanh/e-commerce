package com.dnnthanh.marketplace.be.settlement.api.application.service;

import com.dnnthanh.marketplace.be.platform.context.UserContext;
import com.dnnthanh.marketplace.be.platform.stereotype.UseCase;
import com.dnnthanh.marketplace.be.settlement.api.application.dto.SettlementRebuildResult;
import com.dnnthanh.marketplace.be.settlement.api.application.dto.SettlementResult;
import com.dnnthanh.marketplace.be.settlement.api.application.port.in.SettlementUseCase;
import com.dnnthanh.marketplace.be.settlement.api.application.port.out.SettlementPersistencePort;
import com.dnnthanh.marketplace.be.settlement.api.application.query.SettlementSearchCriteria;
import com.dnnthanh.marketplace.be.settlement.api.domain.exception.SettlementStateConflictException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Settlement lifecycle, hold/dispute and ledger-reconciliation use cases. */
@UseCase
@RequiredArgsConstructor
public class SettlementServiceImplement implements SettlementUseCase {
    private final SettlementPersistencePort persistence;
    private final UserContext user;

    @Override
    public Page<SettlementResult> list(SettlementSearchCriteria criteria, Pageable pageable) {
        return persistence
                .findLatest(criteria, pageable)
                .map(
                        row ->
                                new SettlementResult(
                                        row.settlementNo(),
                                        row.sellerId(),
                                        row.grossAmount(),
                                        row.commissionAmount(),
                                        row.payableAmount(),
                                        row.status()));
    }

    @Override
    public void approve(String settlementNo, String reason) {
        requireReason(reason);
        if (!persistence.approve(settlementNo, user.userId(), reason)) {
            throw new SettlementStateConflictException("Settlement is not OPEN: " + settlementNo);
        }
    }

    @Override
    public void closePeriod(String settlementNo, String reason) {
        requireReason(reason);
        if (!persistence.closePeriod(settlementNo, user.userId(), reason)) {
            throw new SettlementStateConflictException(
                    "Settlement cannot be closed: " + settlementNo);
        }
    }

    @Override
    public void placeHold(String settlementNo, String reason) {
        requireReason(reason);
        if (!persistence.placeHold(settlementNo, user.userId(), reason)) {
            throw new SettlementStateConflictException(
                    "Settlement cannot be held: " + settlementNo);
        }
    }

    @Override
    public void releaseHold(String settlementNo, String reason) {
        requireReason(reason);
        if (!persistence.releaseHold(settlementNo, user.userId(), reason)) {
            throw new SettlementStateConflictException(
                    "Settlement is not ON_HOLD: " + settlementNo);
        }
    }

    @Override
    public SettlementRebuildResult rebuildBalance(String settlementNo) {
        var result = persistence.rebuildBalance(settlementNo);
        return new SettlementRebuildResult(
                result.settlementNo(),
                result.storedPayable(),
                result.ledgerPayable(),
                result.drift());
    }

    private static void requireReason(String reason) {
        if (reason == null || reason.isBlank()) {
            throw new SettlementStateConflictException(
                    "Settlement mutation requires an audit reason");
        }
    }
}
