package com.dnnthanh.marketplace.be.inventory.api.application.port.out;

import com.dnnthanh.marketplace.be.inventory.api.domain.model.StockLedger;
import java.util.Optional;

/** Persistence boundary exposing atomic inventory mutation semantics. */
public interface StockLedgerPort {
    Optional<StockLedger> find(String sku, Long warehouseId);

    StockLedger save(StockLedger ledger);

    boolean reserveAtomically(String requestKey, String sku, Long warehouseId, long quantity);

    void confirmReservation(String requestKey);

    void releaseReservation(String requestKey, String reason);
}
