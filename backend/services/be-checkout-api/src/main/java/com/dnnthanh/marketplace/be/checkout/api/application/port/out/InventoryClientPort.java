package com.dnnthanh.marketplace.be.checkout.api.application.port.out;

import com.dnnthanh.marketplace.be.checkout.api.application.model.CheckoutItemCommand;
import java.util.List;

public interface InventoryClientPort {
    List<String> reserve(String checkoutKey, List<CheckoutItemCommand> items);

    void attachOrder(List<String> keys, String orderNo);

    void release(List<String> keys);
}
