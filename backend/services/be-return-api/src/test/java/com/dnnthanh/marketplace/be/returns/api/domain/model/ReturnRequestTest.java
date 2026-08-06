package com.dnnthanh.marketplace.be.returns.api.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.dnnthanh.marketplace.be.returns.api.domain.enumtype.InventoryDisposition;
import com.dnnthanh.marketplace.be.returns.api.domain.enumtype.ReturnStatus;
import com.dnnthanh.marketplace.be.returns.api.domain.exception.ReturnStateConflictException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Return aggregate tests. */
class ReturnRequestTest {
    @Test
    void refundableAmountComesFromOrderNetSnapshots() {
        var request =
                new ReturnRequest(
                        "R",
                        "O",
                        "U",
                        "damaged",
                        List.of(
                                returnLine(1L, new BigDecimal("90")),
                                returnLine(2L, new BigDecimal("40"))));

        assertEquals(new BigDecimal("130.00"), request.refundableAmount());
    }

    @Test
    void enforcesReceiveInspectAndRefundLifecycle() {
        var request =
                new ReturnRequest(
                        "R", "O", "U", "damaged", List.of(returnLine(1L, BigDecimal.ONE)));

        assertThrows(ReturnStateConflictException.class, () -> request.receive(99L));

        request.approve();
        request.receive(99L);
        request.inspect(
                Map.of(1L, new ReturnRequest.InspectionDecision(1, InventoryDisposition.RESTOCK)));
        request.prepareRefund();

        assertEquals(ReturnStatus.REFUND_PENDING, request.status());
        assertEquals(new BigDecimal("1.00"), request.refundableAmount());
    }

    private ReturnRequest.ReturnLine returnLine(Long orderLineId, BigDecimal refundableUnitAmount) {
        return new ReturnRequest.ReturnLine(
                orderLineId,
                10L,
                100L + orderLineId,
                1,
                0,
                1,
                refundableUnitAmount,
                LocalDateTime.of(2026, 8, 1, 12, 0));
    }
}
