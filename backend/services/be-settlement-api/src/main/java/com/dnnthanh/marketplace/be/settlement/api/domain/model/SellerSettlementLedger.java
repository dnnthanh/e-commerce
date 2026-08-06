package com.dnnthanh.marketplace.be.settlement.api.domain.model;

import com.dnnthanh.marketplace.be.settlement.api.domain.enumtype.LedgerEntryType;
import com.dnnthanh.marketplace.be.settlement.api.domain.enumtype.SettlementStatus;
import com.dnnthanh.marketplace.be.settlement.api.domain.exception.SettlementStateConflictException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** Append-only seller payable ledger plus guarded settlement-period lifecycle. */
public final class SellerSettlementLedger {
    private final Long sellerId;
    private final List<Entry> entries = new ArrayList<>();
    private final Set<String> sourceEventIds = new HashSet<>();
    private SettlementStatus status = SettlementStatus.OPEN;
    private String holdReason;

    public SellerSettlementLedger(Long sellerId) {
        this.sellerId = Objects.requireNonNull(sellerId);
    }

    public boolean append(
            String sourceEventId, LedgerEntryType type, BigDecimal amount, String reference) {
        if (status == SettlementStatus.CLOSED) {
            throw new SettlementStateConflictException("Closed settlement ledger is immutable");
        }
        if (!sourceEventIds.add(sourceEventId)) {
            return false;
        }
        entries.add(new Entry(sourceEventId, type, money(amount), reference));
        return true;
    }

    /** Recomputes seller payable only from immutable ledger entries. */
    public BigDecimal rebuildBalance() {
        return entries.stream()
                .map(Entry::amount)
                .reduce(BigDecimal.ZERO.setScale(2), BigDecimal::add);
    }

    public BigDecimal payableBalance() {
        return rebuildBalance();
    }

    /** Places settlement on operational/dispute hold; payout approval must stop while held. */
    public void placeHold(String reason) {
        if (status == SettlementStatus.CLOSED) {
            throw new SettlementStateConflictException("Closed settlement cannot be held");
        }
        if (reason == null || reason.isBlank()) {
            throw new SettlementStateConflictException("Hold requires an audit reason");
        }
        status = SettlementStatus.ON_HOLD;
        holdReason = reason;
    }

    public void releaseHold() {
        if (status != SettlementStatus.ON_HOLD) {
            throw new SettlementStateConflictException("Settlement is not on hold");
        }
        status = SettlementStatus.OPEN;
        holdReason = null;
    }

    /** Closes a period only when there is no unresolved hold. */
    public void closePeriod() {
        if (status == SettlementStatus.ON_HOLD) {
            throw new SettlementStateConflictException("Settlement on hold cannot be closed");
        }
        if (status == SettlementStatus.CLOSED) {
            return;
        }
        status = SettlementStatus.CLOSED;
    }

    public List<Entry> entries() {
        return List.copyOf(entries);
    }

    public Long sellerId() {
        return sellerId;
    }

    public SettlementStatus status() {
        return status;
    }

    public String holdReason() {
        return holdReason;
    }

    private static BigDecimal money(BigDecimal value) {
        return Objects.requireNonNull(value).setScale(2, RoundingMode.HALF_UP);
    }

    public record Entry(
            String sourceEventId, LedgerEntryType type, BigDecimal amount, String reference) {}
}
