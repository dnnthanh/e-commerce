package com.dnnthanh.marketplace.be.settlement.api.adapter.out.persistence;

import com.dnnthanh.marketplace.be.platform.stereotype.Persistence;
import com.dnnthanh.marketplace.be.settlement.api.application.port.out.SettlementLedgerPort;
import com.dnnthanh.marketplace.be.settlement.api.domain.enumtype.LedgerEntryType;
import com.dnnthanh.marketplace.be.settlement.api.domain.model.SellerSettlementLedger;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.simple.JdbcClient;

/** Oracle append-only seller payable ledger with inbox-backed source-event idempotency. */
@Persistence
@RequiredArgsConstructor
public class JdbcSettlementLedgerPersistenceAdapter implements SettlementLedgerPort {
    private static final String CONSUMER_NAME = "SETTLEMENT_LEDGER";
    private final JdbcClient jdbc;

    @Override
    public SellerSettlementLedger loadOrCreate(Long sellerId) {
        SellerSettlementLedger ledger = new SellerSettlementLedger(sellerId);
        jdbc.sql(
                        """
            SELECT source_event_id,entry_type,amount,reference
            FROM seller_settlement_ledger_entry
            WHERE seller_id=:sellerId ORDER BY id
            """)
                .param("sellerId", sellerId)
                .query(
                        (rs, row) ->
                                new SellerSettlementLedger.Entry(
                                        rs.getString("source_event_id"),
                                        LedgerEntryType.valueOf(rs.getString("entry_type")),
                                        rs.getBigDecimal("amount"),
                                        rs.getString("reference")))
                .list()
                .forEach(
                        entry ->
                                ledger.append(
                                        entry.sourceEventId(),
                                        entry.type(),
                                        entry.amount(),
                                        entry.reference()));
        return ledger;
    }

    @Override
    public SellerSettlementLedger save(SellerSettlementLedger ledger) {
        for (SellerSettlementLedger.Entry entry : ledger.entries()) {
            jdbc.sql(
                            """
              MERGE INTO seller_settlement_ledger_entry target
              USING (SELECT :sourceEventId source_event_id FROM dual) source
              ON (target.source_event_id=source.source_event_id)
              WHEN NOT MATCHED THEN INSERT(
                  seller_id,source_event_id,entry_type,amount,reference,created_at)
              VALUES(:sellerId,:sourceEventId,:entryType,:amount,:reference,SYSTIMESTAMP)
              """)
                    .param("sellerId", ledger.sellerId())
                    .param("sourceEventId", entry.sourceEventId())
                    .param("entryType", entry.type().name())
                    .param("amount", entry.amount())
                    .param("reference", entry.reference())
                    .update();
        }
        return loadOrCreate(ledger.sellerId());
    }

    @Override
    public boolean markSourceEventOnce(String eventId) {
        try {
            jdbc.sql(
                            """
              INSERT INTO inbox_event(consumer_name,event_id,processed_at)
              VALUES(:consumerName,:eventId,:processedAt)
              """)
                    .param("consumerName", CONSUMER_NAME)
                    .param("eventId", eventId)
                    .param("processedAt", LocalDateTime.now())
                    .update();
            return true;
        } catch (DataIntegrityViolationException duplicate) {
            return false;
        }
    }
}
