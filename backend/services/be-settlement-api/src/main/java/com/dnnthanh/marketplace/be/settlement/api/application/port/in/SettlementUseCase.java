package com.dnnthanh.marketplace.be.settlement.api.application.port.in;

import com.dnnthanh.marketplace.be.settlement.api.application.dto.SettlementRebuildResult;
import com.dnnthanh.marketplace.be.settlement.api.application.dto.SettlementResult;
import com.dnnthanh.marketplace.be.settlement.api.application.query.SettlementSearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Inbound application port for settlement lifecycle and reconciliation. */
public interface SettlementUseCase {
    Page<SettlementResult> list(SettlementSearchCriteria criteria, Pageable pageable);

    void approve(String settlementNo, String reason);

    void closePeriod(String settlementNo, String reason);

    void placeHold(String settlementNo, String reason);

    void releaseHold(String settlementNo, String reason);

    SettlementRebuildResult rebuildBalance(String settlementNo);
}
