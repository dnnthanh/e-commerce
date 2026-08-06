package com.dnnthanh.marketplace.be.order.api.application.port.in;

import com.dnnthanh.marketplace.be.order.api.application.command.CancelOrderCommand;
import com.dnnthanh.marketplace.be.order.api.application.command.CreateOrderCommand;
import com.dnnthanh.marketplace.be.order.api.application.command.OrderEventCommand;
import com.dnnthanh.marketplace.be.order.api.domain.model.MarketplaceOrder;
import java.util.List;

public interface OrderCommandUseCase {
    MarketplaceOrder create(CreateOrderCommand command);

    MarketplaceOrder cancel(CancelOrderCommand command);

    MarketplaceOrder markPaid(OrderEventCommand command);

    MarketplaceOrder markFulfilling(OrderEventCommand command);

    MarketplaceOrder markCompleted(OrderEventCommand command);

    List<MarketplaceOrder> expireUnpaidBatch();

    MarketplaceOrder cancelSellerOrder(
            String orderNo,
            Long sellerId,
            com.dnnthanh.marketplace.be.order.api.domain.enumtype.CancellationReason reason);

    MarketplaceOrder manualOverrideCancel(
            String orderNo,
            com.dnnthanh.marketplace.be.order.api.domain.enumtype.CancellationReason reason,
            boolean downstreamCompensationConfirmed);
}
