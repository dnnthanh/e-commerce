package com.dnnthanh.marketplace.be.settlement.worker;

import com.dnnthanh.marketplace.be.platform.stereotype.Persistence;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.annotation.Transactional;

@Persistence
@RequiredArgsConstructor
public class SettlementMaterializer {

    private static final BigDecimal COMMISSION_RATE = new BigDecimal("0.05");

    private final JdbcClient jdbc;

    @Transactional
    public void materialize(String eventId, SellerOrderSnapshot sellerOrder) {
        if (!claim(eventId)) {
            return;
        }

        BigDecimal commissionAmount = sellerOrder.payableAmount().multiply(COMMISSION_RATE);
        BigDecimal sellerPayable = sellerOrder.payableAmount().subtract(commissionAmount);
        String settlementNo =
                "SET-" + sellerOrder.sellerId() + "-" + LocalDateTime.now().toLocalDate();

        jdbc.sql(
                        """
                        MERGE INTO settlement target
                        USING (SELECT :settlementNo settlement_no FROM dual) source
                        ON (target.settlement_no = source.settlement_no)
                        WHEN NOT MATCHED THEN INSERT(
                            settlement_no, seller_id, period_start, period_end,
                            gross_amount, commission_amount, payable_amount,
                            status, created_at, updated_at)
                        VALUES(
                            :settlementNo, :sellerId, SYSTIMESTAMP, SYSTIMESTAMP,
                            0, 0, 0, 'OPEN', SYSTIMESTAMP, SYSTIMESTAMP)
                        """)
                .param("settlementNo", settlementNo)
                .param("sellerId", sellerOrder.sellerId())
                .update();

        Long settlementId =
                jdbc.sql("SELECT id FROM settlement WHERE settlement_no=:settlementNo")
                        .param("settlementNo", settlementNo)
                        .query(Long.class)
                        .single();

        jdbc.sql(
                        """
                        MERGE INTO settlement_line target
                        USING (SELECT :sellerOrderNo seller_order_id FROM dual) source
                        ON (target.seller_order_id = source.seller_order_id)
                        WHEN NOT MATCHED THEN INSERT(
                            settlement_id, seller_order_id, gross_amount,
                            commission_amount, payable_amount)
                        VALUES(
                            :settlementId, :sellerOrderNo, :grossAmount,
                            :commissionAmount, :sellerPayable)
                        """)
                .param("settlementId", settlementId)
                .param("sellerOrderNo", sellerOrder.sellerOrderNo())
                .param("grossAmount", sellerOrder.payableAmount())
                .param("commissionAmount", commissionAmount)
                .param("sellerPayable", sellerPayable)
                .update();

        jdbc.sql(
                        """
                        UPDATE settlement
                        SET gross_amount=(
                                SELECT COALESCE(SUM(gross_amount),0)
                                FROM settlement_line
                                WHERE settlement_id=:settlementId),
                            commission_amount=(
                                SELECT COALESCE(SUM(commission_amount),0)
                                FROM settlement_line
                                WHERE settlement_id=:settlementId),
                            payable_amount=(
                                SELECT COALESCE(SUM(payable_amount),0)
                                FROM settlement_line
                                WHERE settlement_id=:settlementId),
                            updated_at=SYSTIMESTAMP
                        WHERE id=:settlementId
                        """)
                .param("settlementId", settlementId)
                .update();
    }

    private boolean claim(String eventId) {
        try {
            jdbc.sql(
                            """
                            INSERT INTO inbox_event(consumer_name, event_id, processed_at)
                            VALUES('settlement-fulfillment-v2', :eventId, SYSTIMESTAMP)
                            """)
                    .param("eventId", eventId)
                    .update();
            return true;
        } catch (DuplicateKeyException duplicate) {
            return false;
        }
    }

    public record SellerOrderSnapshot(
            Long sellerId,
            String sellerOrderNo,
            BigDecimal grossAmount,
            BigDecimal discountAmount,
            BigDecimal payableAmount,
            String status) {}
}
