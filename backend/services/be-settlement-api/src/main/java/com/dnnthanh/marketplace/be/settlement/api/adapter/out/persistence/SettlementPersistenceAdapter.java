package com.dnnthanh.marketplace.be.settlement.api.adapter.out.persistence;

import com.dnnthanh.marketplace.be.platform.stereotype.Persistence;
import com.dnnthanh.marketplace.be.settlement.api.adapter.out.persistence.entity.SettlementOutboxJpaEntity;
import com.dnnthanh.marketplace.be.settlement.api.adapter.out.persistence.exception.SettlementPersistenceException;
import com.dnnthanh.marketplace.be.settlement.api.adapter.out.persistence.mapper.SettlementPersistenceMapper;
import com.dnnthanh.marketplace.be.settlement.api.adapter.out.persistence.repository.SettlementJpaRepository;
import com.dnnthanh.marketplace.be.settlement.api.adapter.out.persistence.repository.SettlementOutboxJpaRepository;
import com.dnnthanh.marketplace.be.settlement.api.application.port.out.SettlementPersistencePort;
import com.dnnthanh.marketplace.be.settlement.api.application.query.SettlementQueryResult;
import com.dnnthanh.marketplace.be.settlement.api.application.query.SettlementSearchCriteria;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

/**
 * JPA-first Oracle settlement lifecycle adapter.
 *
 * <p>Ordinary lifecycle persistence and outbox rows use Spring Data JPA. Relational list/rebuild
 * queries remain native SQL in the repository. Immutable ledger append/reconciliation workloads
 * stay in the dedicated JDBC ledger adapter because they are SQL-centric financial operations.
 */
@Persistence
@RequiredArgsConstructor
public class SettlementPersistenceAdapter implements SettlementPersistencePort {
    private static final String AUDIT_EVENT = "AUDIT_EVENT";
    private static final String OUTBOX_PENDING = "PENDING";

    private final SettlementJpaRepository settlements;
    private final SettlementOutboxJpaRepository outbox;
    private final SettlementPersistenceMapper mapper;
    private final ObjectMapper json;

    @Override
    @Transactional(readOnly = true)
    public Page<SettlementQueryResult> findLatest(
            SettlementSearchCriteria criteria, Pageable pageable) {
        return settlements.findLatestNative(criteria, pageable).map(mapper::toQueryResult);
    }

    @Override
    @Transactional
    public boolean approve(String settlementNo, String actorId, String reason) {
        if (settlements.approveOpen(settlementNo) == 0) {
            return false;
        }
        insertAudit(settlementNo, actorId, "SETTLEMENT_APPROVED: " + reason);
        return true;
    }

    @Override
    @Transactional
    public boolean closePeriod(String settlementNo, String actorId, String reason) {
        if (settlements.closeOpenOrApproved(settlementNo) == 0) {
            return false;
        }
        insertAudit(settlementNo, actorId, "SETTLEMENT_CLOSED: " + reason);
        return true;
    }

    @Override
    @Transactional
    public boolean placeHold(String settlementNo, String actorId, String reason) {
        if (settlements.placeHold(settlementNo, reason) == 0) {
            return false;
        }
        insertAudit(settlementNo, actorId, "SETTLEMENT_ON_HOLD: " + reason);
        return true;
    }

    @Override
    @Transactional
    public boolean releaseHold(String settlementNo, String actorId, String reason) {
        if (settlements.releaseHold(settlementNo) == 0) {
            return false;
        }
        insertAudit(settlementNo, actorId, "SETTLEMENT_HOLD_RELEASED: " + reason);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public RebuildResult rebuildBalance(String settlementNo) {
        var row = settlements.rebuildBalanceNative(settlementNo);
        if (row == null) {
            throw new SettlementPersistenceException("Settlement not found: " + settlementNo, null);
        }
        var stored = row.getStoredPayable();
        var ledger = row.getLedgerPayable();
        return new RebuildResult(settlementNo, stored, ledger, ledger.subtract(stored));
    }

    private void insertAudit(String settlementNo, String actorId, String reason) {
        try {
            SettlementOutboxJpaEntity event = new SettlementOutboxJpaEntity();
            event.setEventId(UUID.randomUUID().toString());
            event.setAggregateId(settlementNo);
            event.setEventType(AUDIT_EVENT);
            event.setStatus(OUTBOX_PENDING);
            event.setCreatedAt(LocalDateTime.now());
            event.setPayloadJson(
                    json.writeValueAsString(
                            Map.of(
                                    "actorId", actorId,
                                    "actorType", "USER",
                                    "action", "SETTLEMENT_LIFECYCLE_CHANGED",
                                    "resourceType", "SETTLEMENT",
                                    "resourceId", settlementNo,
                                    "sourceService", "be-settlement-api",
                                    "reason", reason)));
            outbox.save(event);
        } catch (Exception failure) {
            throw new SettlementPersistenceException(
                    "Settlement event cannot be serialized or persisted", failure);
        }
    }
}
