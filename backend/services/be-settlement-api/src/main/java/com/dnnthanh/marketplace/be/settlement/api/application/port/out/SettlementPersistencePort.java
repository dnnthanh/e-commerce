package com.dnnthanh.marketplace.be.settlement.api.application.port.out;

import com.dnnthanh.marketplace.be.settlement.api.application.query.SettlementQueryResult;
import com.dnnthanh.marketplace.be.settlement.api.application.query.SettlementSearchCriteria;
import java.math.BigDecimal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Oracle settlement lifecycle/query boundary. */
public interface SettlementPersistencePort {
    Page<SettlementQueryResult> findLatest(SettlementSearchCriteria criteria, Pageable pageable);

    boolean approve(String settlementNo, String actorId, String reason);

    boolean closePeriod(String settlementNo, String actorId, String reason);

    boolean placeHold(String settlementNo, String actorId, String reason);

    boolean releaseHold(String settlementNo, String actorId, String reason);

    RebuildResult rebuildBalance(String settlementNo);

    record RebuildResult(
            String settlementNo,
            BigDecimal storedPayable,
            BigDecimal ledgerPayable,
            BigDecimal drift) {}
}
