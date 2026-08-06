package com.dnnthanh.marketplace.be.inventory.api.application.port.out;

import com.dnnthanh.marketplace.be.inventory.api.domain.model.InventoryBalance;
import com.dnnthanh.marketplace.be.inventory.api.domain.model.Reservation;
import java.time.LocalDateTime;
import java.util.Optional;

public interface InventoryRepositoryPort {

    Optional<Reservation> findReservation(String key);

    Optional<InventoryBalance> findBalance(Long skuId, Long warehouseId);

    boolean updateBalance(InventoryBalance balance);

    void insertReservation(Reservation reservation);

    int expireReservations(LocalDateTime now);
}
