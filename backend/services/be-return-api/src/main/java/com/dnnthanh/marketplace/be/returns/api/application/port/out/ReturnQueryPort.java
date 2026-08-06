package com.dnnthanh.marketplace.be.returns.api.application.port.out;

import com.dnnthanh.marketplace.be.returns.api.application.query.ReturnQueryResult;
import java.util.List;
import java.util.Optional;

/** Read-side return query boundary. */
public interface ReturnQueryPort {
    Optional<ReturnQueryResult> findByKey(String returnKey);

    List<ReturnQueryResult> findByUser(String userId, int limit);

    boolean containsSellerLine(String returnKey, Long sellerId);

    /** Quantity already claimed by non-rejected returns for the same order line. */
    int activeReturnedQuantity(String orderId, Long orderLineId);
}
