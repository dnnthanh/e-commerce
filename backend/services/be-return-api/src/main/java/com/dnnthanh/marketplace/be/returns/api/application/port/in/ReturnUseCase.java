package com.dnnthanh.marketplace.be.returns.api.application.port.in;

import com.dnnthanh.marketplace.be.returns.api.domain.enumtype.ReturnStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Inbound return/refund application boundary. */
public interface ReturnUseCase {
    ReturnResult create(CreateReturnCommand command);

    List<ReturnResult> list();

    ReturnResult approve(String returnKey, Long sellerId);

    ReturnResult reject(String returnKey, Long sellerId, String reason);

    ReturnResult receive(String returnKey, Long warehouseId);

    ReturnResult inspect(String returnKey, List<InspectLineCommand> lines);

    ReturnResult openDispute(String returnKey, String reason);

    ReturnResult resolveDispute(
            String returnKey, boolean accepted, List<InspectLineCommand> lines, String reason);

    ReturnResult refund(String returnKey);

    record CreateReturnCommand(
            String requestKey, String orderNo, List<Long> orderLineIds, String reason) {}

    record InspectLineCommand(Long orderLineId, int acceptedQuantity, String disposition) {}

    record ReturnResult(
            String returnKey,
            String orderId,
            ReturnStatus status,
            BigDecimal refundableAmount,
            Long receivingWarehouseId,
            LocalDateTime createdAt) {}
}
