package com.dnnthanh.marketplace.be.checkout.api.application.port.out;

import com.dnnthanh.marketplace.be.checkout.api.application.model.PricedOrderLine;
import java.math.BigDecimal;
import java.util.List;

public interface OrderClientPort {
    String create(
            String checkoutKey,
            String userId,
            List<PricedOrderLine> lines,
            BigDecimal gross,
            BigDecimal discount);
}
