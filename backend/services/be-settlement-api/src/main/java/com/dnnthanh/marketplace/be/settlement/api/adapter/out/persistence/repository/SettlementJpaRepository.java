package com.dnnthanh.marketplace.be.settlement.api.adapter.out.persistence.repository;

import com.dnnthanh.marketplace.be.settlement.api.adapter.out.persistence.entity.SettlementJpaEntity;
import com.dnnthanh.marketplace.be.settlement.api.adapter.out.persistence.projection.SettlementRebuildProjection;
import com.dnnthanh.marketplace.be.settlement.api.adapter.out.persistence.projection.SettlementSummaryProjection;
import com.dnnthanh.marketplace.be.settlement.api.application.query.SettlementSearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Spring Data boundary for ordinary settlement lifecycle persistence.
 *
 * <p>Search/reporting remains explicit native SQL. Atomic status transitions use native modifying
 * queries so the state predicate and update stay one database statement without dropping down to a
 * raw JdbcClient adapter.
 */
public interface SettlementJpaRepository extends JpaRepository<SettlementJpaEntity, Long> {

    @Query(
            value =
                    """
          SELECT settlement_no AS settlementNo,
                 seller_id AS sellerId,
                 gross_amount AS grossAmount,
                 commission_amount AS commissionAmount,
                 payable_amount AS payableAmount,
                 status AS status
          FROM settlement
          WHERE seller_id = :#{#criteria.sellerId}
          ORDER BY id DESC
          """,
            countQuery =
                    """
          SELECT COUNT(*)
          FROM settlement
          WHERE seller_id = :#{#criteria.sellerId}
          """,
            nativeQuery = true)
    Page<SettlementSummaryProjection> findLatestNative(
            @Param("criteria") SettlementSearchCriteria criteria, Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
            value =
                    """
          UPDATE settlement
             SET status = 'APPROVED', updated_at = SYSTIMESTAMP
           WHERE settlement_no = :settlementNo
             AND status = 'OPEN'
          """,
            nativeQuery = true)
    int approveOpen(@Param("settlementNo") String settlementNo);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
            value =
                    """
          UPDATE settlement
             SET status = 'CLOSED', closed_at = SYSTIMESTAMP, updated_at = SYSTIMESTAMP
           WHERE settlement_no = :settlementNo
             AND status IN ('OPEN', 'APPROVED')
          """,
            nativeQuery = true)
    int closeOpenOrApproved(@Param("settlementNo") String settlementNo);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
            value =
                    """
          UPDATE settlement
             SET status = 'ON_HOLD', hold_reason = :reason, updated_at = SYSTIMESTAMP
           WHERE settlement_no = :settlementNo
             AND status <> 'CLOSED'
          """,
            nativeQuery = true)
    int placeHold(@Param("settlementNo") String settlementNo, @Param("reason") String reason);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
            value =
                    """
          UPDATE settlement
             SET status = 'OPEN', hold_reason = NULL, updated_at = SYSTIMESTAMP
           WHERE settlement_no = :settlementNo
             AND status = 'ON_HOLD'
          """,
            nativeQuery = true)
    int releaseHold(@Param("settlementNo") String settlementNo);

    @Query(
            value =
                    """
          SELECT s.settlement_no AS settlementNo,
                 s.payable_amount AS storedPayable,
                 COALESCE(SUM(l.amount), 0) AS ledgerPayable
          FROM settlement s
          LEFT JOIN seller_settlement_ledger_entry l
            ON l.seller_id = s.seller_id
           AND l.created_at >= s.period_start
           AND l.created_at < s.period_end
          WHERE s.settlement_no = :settlementNo
          GROUP BY s.settlement_no, s.payable_amount
          """,
            nativeQuery = true)
    SettlementRebuildProjection rebuildBalanceNative(@Param("settlementNo") String settlementNo);
}
